/*******************************************************************************
 * Copyright (c) 2014-2015 Stefaan Van Cauwenberge
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0 (the "License"). If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  	 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 *
 * The Initial Developer of the Original Code is
 * Stefaan Van Cauwenberge. Portions created by
 *  the Initial Developer are Copyright (C) 2007-2015 by
 * Stefaan Van Cauwenberge. All Rights Reserved.
 *
 * Contributor(s): none so far.
 *    Stefaan Van Cauwenberge: Initial API and implementation
 *******************************************************************************/
package info.vancauwenberge.designer.enhtrace.viewer.outline.page;


import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.novell.core.Core;

import info.vancauwenberge.designer.enhtrace.Activator;
import info.vancauwenberge.designer.enhtrace.action.OpenDetailAction;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.ILogMessageProvider;
import info.vancauwenberge.designer.enhtrace.api.IPolicySetLogMessage;
import info.vancauwenberge.designer.enhtrace.api.IRootLogMessage.Status;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.IStaticInputListener;
import info.vancauwenberge.designer.enhtrace.editor.input.StaticTraceEditorInput.PolicySteps;
import info.vancauwenberge.designer.enhtrace.editors.DetailedTraceEditor;
import info.vancauwenberge.designer.enhtrace.model.logmessage.StatusLogMessage;
import info.vancauwenberge.designer.enhtrace.util.Util;

/*
 * The outline view of the event details is a paged view.
 * This is the policyFlow page showing the fishbone view
 */
public class DetailedPolicyFlowPage extends Page  implements ISelectionChangedListener, IContentOutlinePage, MouseMoveListener,MouseListener, IMenuListener, IStaticInputListener{

	private static final RGB RGB_FILL_HAS_DATA = new RGB(255,255,125);
	private static final RGB RGB_FILL_HAS_FILTER_DATA = new RGB(109, 180, 213);
	private static final RGB RGB_FILL_HAS_NO_DATA = new RGB(255,255,255);
	private static final RGB RGB_BOUND_SELECTED = new RGB(0,0,255);
	private static final RGB RGB_BOUND_RIGHTSELECTED = new RGB(0,0,125);
	private static final RGB RGB_BOUND_UNSELECTED = new RGB(192,192,192);
	private static final RGB RGB_LABEL = new RGB(0,0,0);

	private static final Pattern applyingPolicyPattern = Pattern.compile("%14C.*%3C");
	private static final Pattern applyingPolicySetPattern = Pattern.compile("^Applying ");
	private class GotoLogmessageAction extends Action {
		private final ILogMessage messageToSelect;

		private GotoLogmessageAction(final String text, final ILogMessage messageToSelect) {
			super(text);
			this.messageToSelect = messageToSelect;
		}

		@Override
		public void run(){
			details.setSelection(new IStructuredSelection() {

				@Override
				public boolean isEmpty() {
					return false;
				}

				@Override
				public List<?> toList() {
					return Arrays.asList(toArray());
				}	

				@Override
				public Object[] toArray() {
					return new ILogMessage[]{messageToSelect};
				}

				@Override
				public int size() {
					return 1;
				}

				@Override
				public Iterator<?> iterator() {
					return toList().iterator();
				}

				@Override
				public Object getFirstElement() {
					return messageToSelect;
				}
			});
			details.getSite().getPage().activate(details);
		}

	}

	private static class PaintData{
		final int[] points;
		final Polygon polygon;
		final int maxX;
		final int minX;
		final int minY;
		final int maxY;
		final String label;
		final Image textImage;

		private PaintData(final int[] points, final String label, final Image textImage){
			this.label = label;
			this.textImage = textImage;
			this.points = points;
			final int[] x=new int[points.length/2];
			final int[]y=new int[points.length/2];
			int minX = Integer.MAX_VALUE;
			int maxX = Integer.MIN_VALUE;
			int minY = Integer.MAX_VALUE;
			int maxY = Integer.MIN_VALUE;
			//Convert the points to 2 ararys, x and ynew int[]{77,55,  77,73,  95,82,  113,73,  113,55,  95,64}
			for (int i = 0; i < (points.length/2); i++) {
				x[i] = points[i*2];
				y[i] = points[(i*2)+1];
				minX = Math.min(minX,x[i]);
				maxX = Math.max(maxX,x[i]);
				minY = Math.min(minY,y[i]);
				maxY = Math.max(maxY,y[i]);
			}
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
			this.polygon = new Polygon(x,y,x.length);
		}
	}

	static final Map<StaticTraceEditorInput.PolicySteps, PaintData> paintdataMap = new EnumMap<StaticTraceEditorInput.PolicySteps, DetailedPolicyFlowPage.PaintData>(StaticTraceEditorInput.PolicySteps.class);
	static{

		paintdataMap.put(StaticTraceEditorInput.PolicySteps.INPUT_TRANSFORM, new PaintData(new int[]{77,55,  77,73,  95,82,  113,73,  113,55,  95,64},"Input", Core.getImage("icons/policyflow/text_input.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.OUTPUT_TRANSFORM, new PaintData(new int[]{117,66,  117,84,  135,75,  153,84,  153,66,  135,57},"Output",Core.getImage("icons/policyflow/text_output.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_SCHEMA_MAP, new PaintData(new int[]{24,89,  24,115,  108,115,  108,89},"Schema Mapping",Core.getImage("icons/policyflow/text_schema.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_SCHEMA_MAP, new PaintData(new int[]{109,89,  109,115,  193,115,  193,89},"Schema Mapping",Core.getImage("icons/policyflow/text_schema.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_EVENT, new PaintData(new int[]{48,145,  48,163,  71,170,  94,163,  94,145,  71,152},"Event",Core.getImage("icons/policyflow/text_event.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_MATCHING, new PaintData(new int[]{48,222,  48,240,  71,247,  94,240,  94,222,  71,229},"Matching",Core.getImage("icons/policyflow/text_matching.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_CREATION, new PaintData(new int[]{48,246,  48,264,  71,271,  94,264,  94,246,  71,253},"Creation",Core.getImage("icons/policyflow/text_creation.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_PLACEMENT, new PaintData(new int[]{48,269,  48,287,  71,294,  94,287,  94,269,  71,276},"Placement",Core.getImage("icons/policyflow/text_placement.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_COMMAND, new PaintData(new int[]{48,299,  48,318,  71,324,  94,317,  94,299,  71,306},"Command",Core.getImage("icons/policyflow/text_command.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_EVENT, new PaintData(new int[]{138,319,  138,337,  161,330,  184,337,  184,319,  161,312},"Event",Core.getImage("icons/policyflow/text_event.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_MATCHING, new PaintData(new int[]{138,261,  138,279,  161,272,  184,279,  184,261,  161,254},"Matching",Core.getImage("icons/policyflow/text_matching.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_CREATION, new PaintData(new int[]{138,236,  138,254,  161,247,  184,254,  184,236,  161,229},"Creation",Core.getImage("icons/policyflow/text_creation.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_PLACEMENT, new PaintData(new int[]{138,211,  138,229,  161,222,  184,229,  184,211,  161,204},"Placement",Core.getImage("icons/policyflow/text_placement.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_COMMAND, new PaintData(new int[]{138,181,  138,199,  161,192,  184,199,  184,181,  161,174},"Command",Core.getImage("icons/policyflow/text_command.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_NOTIFY_FILTER, new PaintData(new int[]{44,334,  48,354,  98,354,  94,334},"Notify",Core.getImage("icons/policyflow/text_notify.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_NOTIFY_FILTER, new PaintData(new int[]{134,145,  138,165,  188,165,  183,145},"Notify",Core.getImage("icons/policyflow/text_notify.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_SYNC_FILTER, new PaintData(new int[]{134,341,  138,361,  188,361,  183,341},"Sync", Core.getImage("icons/policyflow/text_sync.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_SYNC_FILTER, new PaintData(new int[]{44,177,  48,197,  98,197,  93,177},"Sync",Core.getImage("icons/policyflow/text_sync.png")));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SHIM, new PaintData(new int[]{88,1,  89,38,  138,38,  138,1 },"App", null));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_ADD_PROCESSOR, new PaintData(new int[]{71,201,  59,213,  71,225,  83,213 },"Add?",null));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_ADD_PROCESSOR, new PaintData(new int[]{161,280,  149,292,  161,304,  173,292 },"Add?", null));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_ASS_PROCESSOR, new PaintData(new int[]{59,116,  59,130,  83,130,  83,116 },"Ass.", null));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_ASS_PROCESSOR, new PaintData(new int[]{149,116,  149,130,  173,130,  173,116 },"Ass.",null));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_RESET_INJECTION, new PaintData(new int[]{5,302,  5,314,  26,314,  32,308, 26,302},"Inject",null));

		paintdataMap.put(StaticTraceEditorInput.PolicySteps.PUB_STARTUP, new PaintData(new int[]{26,23,  26,41,  72,41,  72,23},"Startup",null));
		paintdataMap.put(StaticTraceEditorInput.PolicySteps.SUB_SHUTDOWN, new PaintData(new int[]{149,23,  149,41,  195,41,  195,23},"Shutdown",null));
	}

	private ScrolledComposite pageContent;
	private Canvas canvas;
	private final DetailedTraceEditor details;
	private final Image imagePageFlow;
	private PolicySteps selectedPolicySet;
	private PolicySteps rightClickSelectedPolicySet;
	private Font canvasFont;
	private final Map<RGB,Color> colorCache = new HashMap<RGB,Color>();

	private final List<StatusLogMessage> nonpolicyStatusses = new ArrayList<StatusLogMessage>();
	private Cursor cursorPointer;
	private MenuManager menuManager;
	private final Image overlayRetry;
	private final Image overlaySuccess;
	private final Image overlayWarning;
	private final Image overlayError;
	private final Image overlayFatal;
	private final StaticTraceEditorInput input;
	private final Map<PolicySteps, Image> policy2ImageMap = new EnumMap<PolicySteps, Image>(PolicySteps.class);

	public DetailedPolicyFlowPage(final DetailedTraceEditor details) {
		this.details = details;
		this.input = ((StaticTraceEditorInput)details.getEditorInput());

		input.addListener(this);
		details.addSelectionChangedListener(this);


		//TODO: somehow reuse the image for multiple detail views.
		ImageDescriptor imageDescr =  Activator.getImageDescriptor("icons/page_flow_shell4.gif");
		this.imagePageFlow = imageDescr.createImage();

		imageDescr =  Activator.getImageDescriptor("icons/overlayRetry.gif");
		this.overlayRetry = imageDescr.createImage();

		imageDescr =  Activator.getImageDescriptor("icons/overlaySuccess.gif");
		this.overlaySuccess = imageDescr.createImage();

		imageDescr =  Activator.getImageDescriptor("icons/overlayWarning.gif");
		this.overlayWarning = imageDescr.createImage();

		imageDescr =  Activator.getImageDescriptor("icons/overlayError.gif");
		this.overlayError = imageDescr.createImage();

		imageDescr =  Activator.getImageDescriptor("icons/overlayFatal.gif");
		this.overlayFatal = imageDescr.createImage();

		createOverlayMap();
	}

	@Override
	public void createControl(final Composite parent){
		pageContent = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		pageContent.setExpandHorizontal(true);
		pageContent.setExpandVertical(false);
		pageContent.setContent(createContent());
		createMenu();
	}

	private void disposeColorCache(){
		for (final Color colors:colorCache.values()){
			colors.dispose();
		}
	}

	private Color getColor(final RGB rgb){
		Color color = colorCache.get(rgb);
		if (color == null){
			color = new Color(canvas.getDisplay(),rgb);
			colorCache.put(rgb, color);
		}
		return color;
	}


	private void createMenu(){
		//Add a context menu extention point
		menuManager =  new  MenuManager ();
		//allow additions => not for this menu. All is added on the fly.
		//menuManager.add ( new  Separator( IWorkbenchActionConstants.MB_ADDITIONS)) ; 
		menuManager.setRemoveAllWhenShown ( true );

		//Control control = getControl();
		final Menu menu = menuManager.createContextMenu(canvas);
		canvas.setMenu(menu);

		menuManager.addMenuListener(this);

		/*
		Control control = getControl();

		Menu menu = new Menu(canvas);
		canvas.setMenu(menu);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
	    item.setText("Popup");*/
	}



	private Control createContent() {
		// * Note: The <code>NO_BACKGROUND</code>, <code>NO_FOCUS</code>, <code>NO_MERGE_PAINTS</code>,
		// * and <code>NO_REDRAW_RESIZE</code> styles are intended for use with <code>Canvas</code>.

		this.canvas = new Canvas(pageContent, SWT.NO_MERGE_PAINTS | SWT.NO_REDRAW_RESIZE);

		final ImageData imageData = imagePageFlow.getImageData();

		final int shellWidth = imageData.width;
		final int shellHeight = imageData.height;
		canvas.setBounds(0, 0, shellWidth, shellHeight);
		cursorPointer = new Cursor(canvas.getDisplay(), SWT.CURSOR_HAND);
		System.out.println("shellWidth:"+shellWidth+"; shellHeight"+shellHeight);
		final Font f = PlatformUI.getWorkbench().getDisplay().getSystemFont();
		final FontData fontdata  = f.getFontData()[0];
		//Take the default system font, but a bit smaller
		this.canvasFont = new Font(canvas.getDisplay(), fontdata.getName(), fontdata.getHeight()-4, fontdata.getStyle());

		canvas.setFont(canvasFont);

		//GC gc = new GC(canvas);

		canvas.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(final PaintEvent e) {
				e.gc.drawImage(imagePageFlow, 0, 0);
				e.gc.setLineCap(SWT.CAP_SQUARE);
				for (final PolicySteps policySet: PolicySteps.values()){
					final PaintData paintData = paintdataMap.get(policySet);
					final List<IPolicySetLogMessage> messageList = input.getPolicy2MessageMap().get(policySet);
					//Fill with "having data colour
					if (messageList != null){
						switch (policySet) {
						case PUB_NOTIFY_FILTER:
						case PUB_SYNC_FILTER:
						case SUB_NOTIFY_FILTER:
						case SUB_SYNC_FILTER:
						case SUB_ADD_PROCESSOR:
						case PUB_ADD_PROCESSOR:
							e.gc.setBackground(getColor(RGB_FILL_HAS_FILTER_DATA));							
							break;
						default:
							e.gc.setBackground(getColor(RGB_FILL_HAS_DATA));
							break;
						}
					} else {
						e.gc.setBackground(getColor(RGB_FILL_HAS_NO_DATA));
					}
					e.gc.fillPolygon(paintData.points);

					if (selectedPolicySet==policySet){
						e.gc.setLineWidth(2);
						e.gc.setForeground(getColor(RGB_BOUND_SELECTED));
						e.gc.drawPolygon(paintData.points);//new int[]{77+1,55+1,  77+1,73,  95,82-1,  113,73-1,  113,55+1,  95,64+1});			    		
					}else if (rightClickSelectedPolicySet==policySet){
						e.gc.setLineWidth(2);
						e.gc.setForeground(getColor(RGB_BOUND_RIGHTSELECTED));
						e.gc.drawPolygon(paintData.points);//new int[]{77+1,55+1,  77+1,73,  95,82-1,  113,73-1,  113,55+1,  95,64+1});			    		
					}else {//UnSelected bounds
						e.gc.setLineWidth(1);
						e.gc.setForeground(getColor(RGB_BOUND_UNSELECTED));
						e.gc.drawPolygon(paintData.points);			    		
					}

					if (paintData.textImage != null){
						final Rectangle strSize = paintData.textImage.getBounds();
						e.gc.drawImage(paintData.textImage, ((paintData.maxX+paintData.minX)-strSize.width)/2, ((paintData.maxY+paintData.minY)-strSize.height)/2);
					}else 		
						//Draw the label
						if (paintData.label != null){
							e.gc.setForeground(getColor(RGB_LABEL));
							final Point strSize = e.gc.stringExtent(paintData.label);
							e.gc.drawText(paintData.label, ((paintData.maxX+paintData.minX)-strSize.x)/2 , ((paintData.maxY+paintData.minY)-strSize.y)/2, true);
						}
					//Draw any overlay if the message has a status as a child
					final Image overlay = policy2ImageMap.get(policySet);
					if (overlay != null){
						e.gc.drawImage(overlay, paintData.minX, paintData.minY);			    		
					}
				}
			}

		});

		canvas.addMouseMoveListener(this);
		canvas.addMouseListener(this);

		return canvas;

	}

	@Override
	public void setFocus(){
		pageContent.setFocus();
	}

	@Override
	public void mouseMove(final MouseEvent e) {
		if (isStatusOverlayPosition(e.x,e.y)){
			canvas.setCursor(cursorPointer);			
		}else{
			final PolicySteps aPolicy = getPolicySets(e.x,e.y);
			if (aPolicy != null){
				canvas.setCursor(cursorPointer);
			}else{
				canvas.setCursor(null);
			}
		}
	}

	@Override
	public Control getControl(){
		return pageContent;
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	private IStructuredSelection createSelection(final ILogMessage statusMessage) {
		return new IStructuredSelection() {

			@Override
			public boolean isEmpty() {
				return false;
			}

			@Override
			public List<?> toList() {
				final List<ILogMessage> result = new ArrayList<ILogMessage>(1);
				result.add(statusMessage);
				return result;
			}

			@Override
			public Object[] toArray() {
				return new Object[]{statusMessage};
			}

			@Override
			public int size() {
				return 1;
			}

			@Override
			public Iterator<?> iterator() {
				return toList().iterator();
			}

			@Override
			public Object getFirstElement() {
				return statusMessage;
			}
		};
	}

	private IStructuredSelection createSelection( final PolicySteps PolicySteps) {
		if (PolicySteps != null){
			final List<IPolicySetLogMessage> result = input.getPolicy2MessageMap().get(PolicySteps);
			System.out.println(this.getClass().getName()+".createSelection():"+result);
			if ((result != null) && (result.size()>0)) {
				return new IStructuredSelection() {

					@Override
					public boolean isEmpty() {
						return false;
					}

					@Override
					public List<?> toList() {
						return result;
					}

					@Override
					public Object[] toArray() {
						return result.toArray();
					}

					@Override
					public int size() {
						return result.size();
					}

					@Override
					public Iterator<?> iterator() {
						return result.iterator();
					}

					@Override
					public Object getFirstElement() {
						return result.get(0);
					}
				};
			}
		}
		System.out.println(this.getClass().getName()+".createSelection(): null");
		return null;
	}


	/**
	 * Redraw the policyflow with the new selection
	 * @param newSelected
	 */
	private void redrawPolicyFlow(final PolicySteps oldSelection, final PolicySteps newSelected) {
		if (newSelected != oldSelection){
			if (newSelected != null){
				final PaintData paintData = paintdataMap.get(newSelected);
				canvas.redraw(paintData.minX-1, paintData.minY-1, paintData.maxX+1, paintData.maxY+1, false);
			}

			if (oldSelection != null){
				final PaintData paintData = paintdataMap.get(oldSelection );
				canvas.redraw(paintData.minX-1, paintData.minY-1, paintData.maxX+1, paintData.maxY+1, false);
			}
		}
	}

	@Override
	public void dispose(){
		canvas.dispose();
		pageContent.dispose();
		imagePageFlow.dispose();
		canvasFont.dispose();
		disposeColorCache();
		menuManager.dispose();
		cursorPointer.dispose();
		overlayError.dispose();
		overlayFatal.dispose();
		overlayRetry.dispose();
		overlaySuccess.dispose();
		overlayWarning.dispose();
		super.dispose();
		super.dispose();
	}

	@Override
	public void mouseDoubleClick(final MouseEvent e) {
		mouseDown(e);		
	}

	private boolean isStatusOverlayPosition(final int x, final int y){
		for (final PolicySteps policySet : PolicySteps.values()) {
			final Image overlay = policy2ImageMap.get(policySet);
			if (overlay != null){
				final Rectangle rect = overlay.getBounds();
				final PaintData paintData = paintdataMap.get(policySet);
				rect.x = paintData.minX;
				rect.y = paintData.minY;
				if (rect.contains(x, y)){
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Get the worst case status message for the policy set under the given x-y coordinates
	 * @param x
	 * @param y
	 * @return
	 */
	private StatusLogMessage getWorstStatusMessage(final int x, final int y){
		for (final PolicySteps policySet : PolicySteps.values()) {
			final Image overlay = policy2ImageMap.get(policySet);
			if (overlay != null){
				final PaintData paintData = paintdataMap.get(policySet);
				final Rectangle rect = overlay.getBounds();
				rect.x = paintData.minX;
				rect.y = paintData.minY;
				if (rect.contains(x, y)){
					Status worstStatus = Status.UNKNOWN;
					StatusLogMessage worstStatusMessage = null;

					final List<IPolicySetLogMessage> messageList = input.getPolicy2MessageMap().get(policySet);

					for (final IPolicySetLogMessage iPolicySetLogMessage : messageList) {
						final List<StatusLogMessage> allStatusses = input.getPolicy2StatusMap().get(iPolicySetLogMessage);
						if (allStatusses != null){
							for (final StatusLogMessage statusLogMessage : allStatusses) {
								if (worstStatus.getLevel() < statusLogMessage.getStatus().getLevel()) {
									worstStatusMessage = statusLogMessage;
								}
								worstStatus = statusLogMessage.getStatus();
							}
						}
					}
					return worstStatusMessage;
				}
			}
		}
		return null;
	}

	/**
	 * Get the policyset (if any) at the given coordinates
	 * @param x
	 * @param y
	 * @return
	 */
	private PolicySteps getPolicySets(final int x, final int y){
		for (final PolicySteps policySet : PolicySteps.values()) {
			final PaintData paintData = paintdataMap.get(policySet);
			if (paintData.polygon.contains(x,y)){
				return policySet;
			}
		}
		return null;
	}

	@Override
	public void mouseDown(final MouseEvent e) {
		switch (e.button) {
		case 1:
			redrawPolicyFlow(rightClickSelectedPolicySet, null);
			rightClickSelectedPolicySet = null;

			final StatusLogMessage statusMessage = getWorstStatusMessage(e.x,e.y);
			if (statusMessage != null){
				System.out.println("mouseDown(): Setting selection to "+statusMessage);
				details.setSelection(createSelection(statusMessage));
			}else{
				final PolicySteps aPolicy = getPolicySets(e.x,e.y);
				System.out.println("mouseDown(): Setting selection to "+aPolicy);
				details.setSelection(createSelection(aPolicy));
			}
			details.getSite().getPage().activate(details);
			break;
		case 3:
			final PolicySteps oldRightClickSelection = rightClickSelectedPolicySet;
			rightClickSelectedPolicySet = getPolicySets(e.x,e.y);
			redrawPolicyFlow(oldRightClickSelection, rightClickSelectedPolicySet);
			break;
		default:
			break;
		}
	}

	@Override
	public void mouseUp(final MouseEvent e) {
		//Nothing to do
	}

	@Override
	public void menuAboutToShow(final IMenuManager manager) {
		System.out.println("about to show. selectedPolicySet="+rightClickSelectedPolicySet);
		if (rightClickSelectedPolicySet != null){
			final List<IPolicySetLogMessage> rootMessages = input.getPolicy2MessageMap().get(rightClickSelectedPolicySet);
			if ((rootMessages != null) && (rootMessages.size()>0)){
				if (rootMessages.size()>1){
					for (final IPolicySetLogMessage iPolicySetLogMessage : rootMessages) {
						String label = null;
						if (iPolicySetLogMessage.getPolicySet().isSubflowRoot()){
							//If this is the root, the label should come from itself
							label = iPolicySetLogMessage.getMessage();							
						} else {
							//If this is not the root, the parent should have the label
							label = iPolicySetLogMessage.getParent().getMessage();
						}
						//TODO: make this more generic.
						//Potentially more strings can be removed.
						label = label.replaceAll("^Subscriber processing ", "");
						label = label.replaceAll("^Publisher processing ", "");
						final MenuManager subMenu = new MenuManager(Util.elipsisLabelMiddle(label, Util.MAX_LABEL_SIZE), null);				
						manager.add(subMenu);

						createPolicysetMenu(iPolicySetLogMessage, subMenu);

					}					
				}else{
					//Do not create a submenu if only one policymessage in the list
					createPolicysetMenu(rootMessages.get(0), menuManager);
				}
			}

		}else{
			if ((nonpolicyStatusses != null) && (nonpolicyStatusses.size()>0)) {
				createStatusSubmenu(manager, nonpolicyStatusses);
			}
		}
	}

	private void createPolicysetMenu(final IPolicySetLogMessage iPolicySetLogMessage,
			final MenuManager subMenu) {
		createPolicysetSubmenu(subMenu, iPolicySetLogMessage);

		final List<StatusLogMessage> statusses = input.getPolicy2StatusMap().get(iPolicySetLogMessage);
		if ((statusses != null) && (statusses.size()>0)){				
			createStatusSubmenu(subMenu, statusses);
		}
		final List<ILogMessage> traces = input.getPolicy2TraceMap().get(iPolicySetLogMessage);
		if ((traces != null) && (traces.size()>0)) {
			createTracesSubmenu(subMenu, traces);
		}

		if (iPolicySetLogMessage.getPolicySet().isSubflowRoot()){

			subMenu.add(new OpenDetailAction(new IStructuredSelection() {

				@Override
				public boolean isEmpty() {
					return false;
				}

				@Override
				public List<?> toList() {
					return Arrays.asList(toArray());
				}

				@Override
				public Object[] toArray() {
					return new Object[]{iPolicySetLogMessage};
				}

				@Override
				public int size() {
					return 1;
				}

				@Override
				public Iterator<?> iterator() {
					return toList().iterator();
				}

				@Override
				public Object getFirstElement() {
					return iPolicySetLogMessage;
				}
			}, details));
		}
	}

	private void createTracesSubmenu(final IMenuManager manager,
			final List<ILogMessage> traces) {
		final MenuManager subMenu = new MenuManager("Trace", null);				
		for (final ILogMessage iLogMessage : traces) {
			subMenu.add(new GotoLogmessageAction(Util.elipsisLabelEnd(iLogMessage.getMessage(), Util.MAX_LABEL_SIZE), iLogMessage));			
		}
		manager.add(subMenu);
	}

	private void createPolicysetSubmenu(final IMenuManager manager, final IPolicySetLogMessage iPolicySetLogMessage) {
		if (iPolicySetLogMessage.hasChildren()){
			final List<ILogMessage> children = iPolicySetLogMessage.getChildren();
			MenuManager subMenu = null;
			if (iPolicySetLogMessage.getPolicySet().isSubflowRoot()){
				//Only list the policySets
				subMenu = new MenuManager("Policy Set", null);
				for (final ILogMessage iLogMessage : children) {
					if (iLogMessage instanceof IPolicySetLogMessage){
						final String message = iLogMessage.getMessage();
						/*
						Matcher matcher = applyingPolicySetPattern.matcher(message);

						if (matcher.find()){
							message = message.substring(matcher.end());
						}*/
						subMenu.add(new GotoLogmessageAction(Util.elipsisLabelEnd(message,Util.MAX_LABEL_SIZE), iLogMessage));						
					}
				}

			}else{
				subMenu = new MenuManager("Policy", null);
				for (final ILogMessage iLogMessage : children) {
					String message = iLogMessage.getMessage();

					if (message.startsWith("Applying policy:") || message.startsWith("Applying XSLT policy:")){
						//We need to create a submenu
						final Matcher matcher = applyingPolicyPattern.matcher(message);

						if (matcher.find()){
							message = message.substring(matcher.start()+4,matcher.end()-3);
						}
						subMenu.add(new GotoLogmessageAction(Util.elipsisLabelEnd(message,Util.MAX_LABEL_SIZE), iLogMessage));
					}
				}
			}
			if (!subMenu.isEmpty()) {
				manager.add(subMenu);
			}
		}
	}

	private void createStatusSubmenu(final IMenuManager manager, final List<StatusLogMessage> statusses) {
		final MenuManager subMenu = new MenuManager("Status", null);				
		for (final StatusLogMessage statusLogMessage : statusses) {
			final StringBuilder sbLabel = new StringBuilder();
			sbLabel.append(statusLogMessage.getStatus().geLabel());
			final String message = statusLogMessage.getStatusMessage();
			if (message != null){
				sbLabel.append(": ");
				sbLabel.append(message);
			}
			subMenu.add(new GotoLogmessageAction(Util.elipsisLabelEnd(sbLabel.toString(),Util.MAX_LABEL_SIZE),statusLogMessage));			
		}
		manager.add(subMenu);
	}








	/*
	private void buildNonpolicyStatusList(ILogMessage root) {
		//No need to analyze if this message is a policy root
		Collection<List<IPolicySetLogMessage>> valueList = policy2MessageMap.values();
		//TODO: buggy but we no longer have statusses without a parent policy....
		for (List<IPolicySetLogMessage> list : valueList) {
			if (list.contains(root))
				return;
			if (root instanceof StatusLogMessage)
				nonpolicyStatusses.add((StatusLogMessage)root);
			if (root.hasChildren()){
				List<ILogMessage> children = root.getChildren();
				for (ILogMessage iLogMessage : children) {
					buildNonpolicyStatusList(iLogMessage);
				}
			}

		}
	}*/

	private Image getOverlayFor(final Status worstStatus) {
		switch (worstStatus) {
		case WARNING:
			return overlayWarning;
		case SUCCESS:
			return overlaySuccess;
		case RETRY:
			return overlayRetry;
		case ERROR:
			return overlayError;
		case FATAL:
			return overlayFatal;
		default:
			return null;
		}
	}

	private void createOverlayMap(){
		//Overlays are again per policy set, based on the statusses found before
		final Set<StaticTraceEditorInput.PolicySteps> keys = input.getPolicy2MessageMap().keySet();
		for (final StaticTraceEditorInput.PolicySteps policySets : keys) {
			final List<IPolicySetLogMessage> messageList = input.getPolicy2MessageMap().get(policySets);
			Status worstStatus = Status.UNKNOWN;
			for (final IPolicySetLogMessage iPolicySetLogMessage : messageList) {
				final List<StatusLogMessage> allStatusses = input.getPolicy2StatusMap().get(iPolicySetLogMessage);
				if (allStatusses != null) {
					worstStatus = getWorstStatus(worstStatus, allStatusses);
				}				
			}
			final Image overlay = getOverlayFor(worstStatus);
			if (overlay != null){
				policy2ImageMap.put(policySets, overlay);
			}
		}		
	}

	private Status getWorstStatus(Status currentWorstStatus, final List<StatusLogMessage> allStatusses) {
		for (final StatusLogMessage statusLogMessage : allStatusses) {
			if (currentWorstStatus.getLevel() < statusLogMessage.getStatus().getLevel()) {
				currentWorstStatus = statusLogMessage.getStatus();
			}
		}
		return currentWorstStatus;
	}


	public void refresh() {
		createOverlayMap();
		if (canvas != null) {
			canvas.redraw();
		}
	}

	@Override
	public void notifyRootChanged(final ILogMessage newValue, final ILogMessage oldValue) {
		createOverlayMap();
		canvas.redraw();
	}

	/**
	 * The selection in the editor changed.
	 */
	@Override
	public void selectionChanged(final SelectionChangedEvent event) {
		//Remove any right click selection when we are receiving a new selection
		rightClickSelectedPolicySet = null;
		redrawPolicyFlow(rightClickSelectedPolicySet, null);

		//Now get the actual selection (if any)
		final ISelection selection = event.getSelection();
		if (selection == null){
			return;
		}

		System.out.println(this.getClass().getName()+ ".selectionChanged() to "+selection);
		if (selection instanceof IStructuredSelection){
			final Object selectedObject = ((IStructuredSelection) selection).getFirstElement();
			System.out.println(this.getClass().getName()+ ".selectionChanged() to "+selectedObject);
			if (selectedObject == null){
				redrawPolicyFlow(selectedPolicySet, null);
				selectedPolicySet=null;
				return;				
			}

			if (selectedObject instanceof ILogMessageProvider){
				ILogMessage message = ((ILogMessageProvider) selectedObject).getLogMessage();
				while(true){
					if (message instanceof IPolicySetLogMessage){
						final Set<StaticTraceEditorInput.PolicySteps> keys = input.getPolicy2MessageMap().keySet();
						for (final StaticTraceEditorInput.PolicySteps policySets : keys) {
							final List<IPolicySetLogMessage> listOfPolicies = input.getPolicy2MessageMap().get(policySets);
							if (listOfPolicies.contains(message)){
								redrawPolicyFlow(selectedPolicySet, policySets);
								//Update the selection
								selectedPolicySet = policySets;
								return;
							}
						}
					}
					message = message.getParent();
					if (message==null){
						redrawPolicyFlow(selectedPolicySet, null);
						selectedPolicySet=null;
						return;
					}
				}
			}
		}
	}

	@Override
	public void addSelectionChangedListener(final ISelectionChangedListener listener) {
		//We are not a selection provider....
	}

	@Override
	public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
		//We are not a selection provider....
	}

	@Override
	public void setSelection(final ISelection selection) {
		//We are not a selection provider....
	}


}
