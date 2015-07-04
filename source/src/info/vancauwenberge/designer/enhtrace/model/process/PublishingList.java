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
package info.vancauwenberge.designer.enhtrace.model.process;

import info.vancauwenberge.designer.enhtrace.api.IListListener;
import info.vancauwenberge.designer.enhtrace.api.ILogMessage;
import info.vancauwenberge.designer.enhtrace.api.IRootLogMessage;
import info.vancauwenberge.designer.enhtrace.model.logmessage.LogMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;


public class PublishingList  {
	private static final int MAX_QUEUESIZE = 10000;
	
	/**
	 * The list of items that are added one by one. Every xxx miliseconds a listchange event will be sent out with these events
	 * in order to get a bulk update effect (performance!).
	 */
	private ArrayList<ILogMessage> unpublishedList = new ArrayList<ILogMessage>();

	/** 
	 * Used for smart clean-up of messages
	 */
	private Map<String, IRootLogMessage> lastRootsMap = new HashMap<String, IRootLogMessage>(); 

	/**
	 * All log messages we have.
	 */
	private ArrayList<ILogMessage> publishedList = new ArrayList<ILogMessage>();

	private LinkedList<IListListener<ILogMessage>> listListeners = new LinkedList<IListListener<ILogMessage>>();

	private EventCleanerThread cleanThread;

	private PublisherThread bulkNotifyThread;

	
	
	private class EventCleanerThread extends Thread{
		private boolean isStoped=false;

		public EventCleanerThread(){
			super();
			setDaemon(true);
			setName("DSTraceEventQueueCleaner");			
		}
		
		@Override
		public void run() {
			while(!isStoped){
				synchronized (this) {
					try {
						//We will check every second to see what the size of the queue is.
						this.wait(1000);
					} catch (InterruptedException e) {
						isStoped = true;
						System.out.println(this.getClass().getName()+" interrupted! Thread will exit.");
					}
					if (!isStoped){
						//No need to synchronize yet
						if (publishedList.size() > MAX_QUEUESIZE){
							cleanMessages();
						}
					}
				}
			}
			
		}

		public void stopThread() {
			synchronized (this) {
				isStoped = true;
				this.notifyAll();
			}
		}
		
		/**
		 * The queue has grown to big. We need to clean up. Called from the cleaner thread.
		 */
		private void cleanMessages(){
			System.out.println("");
			System.out.println("==============================");
			System.out.println("");

			Collection<IRootLogMessage> lastRoots = lastRootsMap.values();
			
			
			
			
			//lock any adding or removing of events until after the clean is performed.
			synchronized (publishedList) {
				int itemsToCollect = publishedList.size()-MAX_QUEUESIZE;
				Collection<ILogMessage> removedEvents = new ArrayList<ILogMessage>(itemsToCollect);
				Iterator<ILogMessage> iter = publishedList.iterator();
				
				while (removedEvents.size()<itemsToCollect && iter.hasNext()){
					ILogMessage next = iter.next();
					//Do not add if the root of the message is in the list of last roots:
					//We do not delete the last root of a given trace/thread
					//if (lastRoots.contains(next))
					//	continue;
					if (lastRoots.contains(next.getRootMessage()))
						continue;
					removedEvents.add(next);							
				}
				
				//As long as we did not collect enough events, extend the list
/*				while (elements.size() - removedEvents.size()>MAX_QUEUESIZE){
					//We should have at least one event...
					//get the next not-yet-taged-for-removing event
					ILogMessage oldestMessage =null;
					Iterator<ILogMessage> iter = elements.iterator();
					while (oldestMessage == null && iter.hasNext()){
						ILogMessage next = iter.next();
						if (!removedEvents.contains(next))
							oldestMessage = next;
					}
					
					removedEvents.add(oldestMessage);
					getSubtreeItems(oldestMessage,removedEvents);
				}*/
				
				
				//Do the actual remove
				boolean result = publishedList.removeAll(removedEvents);

				if (result){
					ILogMessage[] removedArray = new ILogMessage[removedEvents.size()];
					notifyItemsRemoved(removedEvents.toArray(removedArray));
				}
			}
			//remove(removedEvents);
			//System.out.println("Size of the queue:"+ObjectSizeFetcher.getObjectSize(this));
			System.out.println("");
			System.out.println("==============================");
			System.out.println("");
		}
			
		/**
		 * This method does not sent out notifications. The caller is responsible for sending out the notifications
		 * The removed logEvents are added to the removedEvents collection.
		 * Note: It is assumed that the caller has a lock on the events object!!!
		 * @param root
		 */
		private void getSubtreeItems(ILogMessage root, Collection<ILogMessage> subtreeItems){
			if (root.hasChildren()){
				List<ILogMessage> children = root.getChildren();
				subtreeItems.addAll(children);
				for (ILogMessage logEvent : children) {
					getSubtreeItems(logEvent,subtreeItems);
				}
			}
		}
		
	}

	
	/**
	 * This threads publiches the unpublished events. It sends out the notification for te items added.
	 * The publishing is done once a second.
	 * @author stefaanv
	 *
	 */
	private class PublisherThread extends Thread{
		private boolean isStoped=false;
		public PublisherThread(){
			super("BulkAddThread");
			setDaemon(true);
		}
		
		
		public void stopThread(){
			this.isStoped = true;
		}


		public void run() {
			while(!isStoped){
				try {
					synchronized (this) {
						//We update once a second
						this.wait(1000);
					}
					
					if(!isStoped){
						ILogMessage[] newItems = null;
						synchronized (unpublishedList) {
							if (unpublishedList.size()>0){
								newItems = new ILogMessage[unpublishedList.size()];
								newItems = unpublishedList.toArray(newItems);

								synchronized (publishedList) {
									publishedList.addAll(unpublishedList);
									notifyItemsAdded(newItems);
								}
								unpublishedList.clear();
							}
						}
					}
				} catch (InterruptedException e) {
					System.out.println(this.getClass().getName()+" interrupted! Thread will exit.");
					isStoped = true;
					e.printStackTrace();
				} catch (Exception e){
					e.printStackTrace();					
				}
			}
		}

	}

	
	
	/**
	 * Create the publishing list, starting the publisher and cleaner threads.
	 */
	public PublishingList(){
		this.cleanThread = new EventCleanerThread();
		cleanThread.start();
		
		this.bulkNotifyThread = new PublisherThread();
		bulkNotifyThread.start();
	}

	
	
	private String getLastMessageMapKey(ILogMessage message) {
		return getLastMessageMapKey(message.getTraceName(),message.getThread());
	}
	
	private String getLastMessageMapKey(String trace, String thread) {
		return trace+thread;
	}

	public void addListListener(IListListener<ILogMessage> listener) {
		synchronized (listListeners) {
			listListeners.add(listener);			
		}

	}

	public void removeListListener(IListListener<ILogMessage> listener) {
		synchronized (listListeners) {
			listListeners.remove(listener);			
		}
	}
	
	/**
	 * Disposes without sending out a notification that the lists are cleared.
	 */
	public void dispose(){
		
		cleanThread.stopThread();
		synchronized (cleanThread) {
			cleanThread.notifyAll();
		}
		
		bulkNotifyThread.stopThread();
		synchronized (bulkNotifyThread) {
			bulkNotifyThread.notifyAll();
		}
		
		synchronized (unpublishedList) {
			unpublishedList.clear();
			synchronized (publishedList) {
				publishedList.clear();				
			}
		}
	}

	private void notifyItemsRemoved(ILogMessage[] list){
		synchronized (listListeners) {
			for (IListListener<ILogMessage> listener : listListeners) {
				try{
					listener.itemsRemoved(list);
				}catch (Throwable th) {
					th.printStackTrace();
				}
			}
		}

	}
	
	private void notifyItemsAdded(ILogMessage[] list){
		
		
		synchronized (listListeners) {
			for (IListListener<ILogMessage> listener : listListeners) {
				try{
					listener.itemsAdded(list);
				}catch (Throwable th) {
					th.printStackTrace();
				}
			}
		}
		

	}

	
	private void notifyListCleared(){
		synchronized (listListeners) {
			for (IListListener<ILogMessage> listener : listListeners) {
				try{
					listener.listClreared();
				}catch (Throwable th) {
					th.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Adds an item to the unpublished list of items.
	 * @param message
	 * @return
	 */
	public void add(ILogMessage[] messages) {
		synchronized (unpublishedList) {
			for (ILogMessage iLogMessage : messages) {
				System.out.println("Adding:"+iLogMessage);
				unpublishedList.add(iLogMessage);
				if (iLogMessage instanceof IRootLogMessage){
					lastRootsMap.put(getLastMessageMapKey(iLogMessage), (IRootLogMessage)iLogMessage);
				}				
			}
		}
	}

	
	/**
	 * Clear both the published and unpublished list of items. Sends out clear notifications.
	 */
	public void clear() {
		synchronized (unpublishedList) {
			unpublishedList.clear();			
			synchronized (publishedList) {
				publishedList.clear();
			}
		}
		notifyListCleared();
	}



	
	/**
	 * Get the last message in the given list for the given thread and trace name.
	 * This method does not lock the list. Locking must be done by the caller!!!
	 * @param traceName
	 * @param thread
	 * @return
	 */
	private static ILogMessage getLastMessage(List<ILogMessage> list, String traceName, String thread) {
		if (list.size()>0){
			ListIterator<ILogMessage> iter = list.listIterator(list.size());
			while (iter.hasPrevious()){
				LogMessage previous = (LogMessage) iter.previous();
				if ((previous.getThread() == thread) && (previous.getTraceName() == traceName)){
					return previous;
				}
			}
		}
		return null;		
	}
	
	/**
	 * Search for the last message, published or not, for the given thread and trace name
	 * @param traceName
	 * @param thread
	 * @return
	 */
	public ILogMessage getLastMessage(String traceName, String thread) {
		//First, search in the unpublished items
		synchronized (unpublishedList) {
			ILogMessage result = getLastMessage(unpublishedList, traceName, thread);
			if (result==null){
				//Search in the published list if not found
				synchronized (publishedList) {
					result = getLastMessage(publishedList, traceName, thread);
				}
			}
			return result;
		}
	}
	
	
	/**
	 * Get the event with details that is 'near' the given eventData 
	 * @param theList
	 * @param dsTime
	 * @param eventData
	 * @return
	 */
	/*
	public ILogMessage getClosestDetailMessage(DebugEventData eventData){
		
		//We need to find the original message and append the string to it
		//We only have the DSTime as a reference...
		long dsTime = eventData.getDsTime() * 1000L + eventData.getMilliSeconds();
		
		//First look in the unpublished list. These are the last messages
		synchronized (unpublishedList) {
			ILogMessage result = getClosest(unpublishedList, dsTime, eventData);
			if (result==null){
				//Nothing found in the unpublished list, search in the publiched list
				synchronized (publishedList) {
					result = getClosest(publishedList, dsTime, eventData);
				}
			}
			return result;			
		}
	}*/

	/**
	 * Get the event with details that is 'near' the given eventData 
	 * @param theList
	 * @param dsTime
	 * @param eventData
	 * @return
	 */
	/*
	private static ILogMessage getClosest(List<ILogMessage> theList, long dsTime, DebugEventData eventData) {
		ListIterator<ILogMessage> iter = theList.listIterator(theList.size());
		while (iter.hasPrevious()){
			LogMessage previous = (LogMessage) iter.previous();
			if (previous.getTimeStamp() == dsTime || previous.getMessageDetail() != null){
				return previous;
			}else{
				return null;
			}
		}
		return null;
	}
	*/

	public int size() {
		return publishedList.size();
	}


	/**
	 * Get the published messages. This list should not be modified. Iterators are not thread safe!
	 */
	public ILogMessage[] getPublishedMessages(){
		synchronized (publishedList) {
			ILogMessage[] result = new ILogMessage[publishedList.size()];
			return publishedList.toArray(result);
			
		}
	}



	/**
	 * Get the message last added to the list, published or not.
	 * @return
	 */
	public ILogMessage getLastMessage() {
		//We need to find the original message and append the string to it
		//First look in the unpublished list. These are the last messages
		synchronized (unpublishedList) {
			if (unpublishedList.size()>0)
				return unpublishedList.get(unpublishedList.size()-1);
			//Nothing found in the unpublished list, search in the publiched list
			synchronized (publishedList) {
				if (publishedList.size()>0)
					return publishedList.get(publishedList.size()-1);
			}
			return null;			
		}
	}

}
