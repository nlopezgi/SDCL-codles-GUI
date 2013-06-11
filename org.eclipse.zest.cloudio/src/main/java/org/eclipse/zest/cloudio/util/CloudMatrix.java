/*******************************************************************************
 * Copyright (c) 2011 Stephan Schwiebert. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.zest.cloudio.util;

import java.util.List;

import org.eclipse.zest.cloudio.Word;
import org.eclipse.zest.cloudio.util.RectTree.RectNode;

/**
 * This class contains all global information about the drawable area and the
 * layouted words in form of a {@link RectTree}.
 * 
 * @author sschwieb
 * 
 */
public class CloudMatrix {

	public RectTree tree;

	private final int max;

	private final int minResolution;

	public int getMinResolution() {
		return minResolution;
	}

	public CloudMatrix(int maxSize, int minResolution) {
		this.max = maxSize;
		this.minResolution = minResolution;
		reset();
	}

	public short get(int x, int y) {

		short ret = tree.getRoot().getWordId(x * minResolution,
				y * minResolution);
		return ret;
	}

	public boolean isEmpty(int x, int y) {
		short id = tree.getRoot().getWordId(x * minResolution,
				y * minResolution);
		return id == RectTree.EMPTY;
	}

	public void reset() {
		SmallRect root = new SmallRect(0, 0, max, max);
		tree = new RectTree(root, minResolution);
	}

	public void set(RectNode node, short id, short xOffset, short yOffset,
			int minResolution, Word word) {
		int cleanX = ((xOffset + node.rect.x) / minResolution) * minResolution;
		int cleanY = ((yOffset + node.rect.y) / minResolution) * minResolution;
		SmallRect rect = new SmallRect(cleanX, cleanY, minResolution,
				minResolution);
		tree.insert(rect, id, word);
	}

	/**
	 * METHODS FOR TAG CLOUD TRANSFORMATION
	 * 
	 */

	public void removeWords(List<Word> wordsToRemove) {
		for (Word word : wordsToRemove) {

			findAndEmptyRectNodesByWord(word, tree.getRoot());
		}
	}

	public void findAndEmptyRectNodesByWord(Word word, RectNode startFrom) {
		// base case, the current node is the one that contains the word
		if (startFrom.containedWord != null
				&& startFrom.containedWord.equals(word)) {

			startFrom.filled = RectTree.EMPTY;
			startFrom.containedWord = null;

		} else {
			if (startFrom.children != null) {
				// recursive, remove word from children
				for (int i = 0; i < startFrom.children.length; i++) {
					if (startFrom.children[i] != null) {
						findAndEmptyRectNodesByWord(word, startFrom.children[i]);
					}
				}
			}
		}
	}
}
