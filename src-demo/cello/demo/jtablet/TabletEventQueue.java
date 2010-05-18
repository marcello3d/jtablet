/*!
 * Copyright (c) 2009 Marcello Bastéa-Forte (marcello@cellosoft.com)
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *     1. The origin of this software must not be misrepresented; you must not
 *     claim that you wrote the original software. If you use this software
 *     in a product, an acknowledgment in the product documentation would be
 *     appreciated but is not required.
 * 
 *     2. Altered source versions must be plainly marked as such, and must not be
 *     misrepresented as being the original software.
 * 
 *     3. This notice may not be removed or altered from any source
 *     distribution.
 */

package cello.demo.jtablet;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cello.jtablet.event.TabletEvent;
import cello.jtablet.event.TabletFunneler;

/**
 * A TabletListener that queues up events for retrieval on a separate thread. 
 * 
 * @author marcello
 */
public class TabletEventQueue extends TabletFunneler {

	private final BlockingQueue<TabletEvent> queue;

	/**
	 * Constructs a new queue with no capacity limit
	 */
	public TabletEventQueue() {
		queue = new LinkedBlockingQueue<TabletEvent>();
	}
	/**
	 * Constructs a new queue with a fixed capacity limit 
	 * @param capacity
	 */
	public TabletEventQueue(int capacity) {
		queue = new LinkedBlockingQueue<TabletEvent>(capacity);
	}
	
	@Override
	protected void handleEvent(TabletEvent ev) {
		queue.add(ev);
	}
	
	/**
	 * @return the next available TabletEvent, if necessary, blocking until one is available 
	 * @throws InterruptedException
	 * @see BlockingQueue#take()
	 */
	public TabletEvent take() throws InterruptedException {
		return queue.take();
	}

}
