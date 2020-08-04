package dev.xframe.utils;

import java.util.Arrays;

public class XBitSet {

	private final static int ADDRESS_BITS_PER_WORD = 6;
	private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;

	private long[] words;

	private static int wordIndex(int bitIndex) {
		return bitIndex >> ADDRESS_BITS_PER_WORD;
	}

	public XBitSet(int nbits) {
		// nbits can't be negative; size 0 is OK
		if (nbits < 0)
			throw new NegativeArraySizeException("nbits < 0: " + nbits);
		initWords(nbits);
	}

	private void initWords(int nbits) {
		words = new long[wordIndex(nbits - 1) + 1];
	}

	private void ensureCapacity(int wordsRequired) {
		if (words.length < wordsRequired) {
			words = Arrays.copyOf(words, wordsRequired);
		}
	}

	private void expandTo(int wordIndex) {
		int wordsRequired = wordIndex + 1;
		ensureCapacity(wordsRequired);
	}
	
	public void ensureCap(int bitIndex) {
		expandTo(wordIndex(bitIndex));
	}

	public void set(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);
		expandTo(wordIndex);

		words[wordIndex] |= (1L << bitIndex); // Restores invariants
	}

	public void set(int bitIndex, boolean value) {
		if (value)
			set(bitIndex);
		else
			clear(bitIndex);
	}

	public void clear(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);
		if (wordIndex >= words.length)
			return;

		words[wordIndex] &= ~(1L << bitIndex);
	}

	public boolean get(int bitIndex) {
		if (bitIndex < 0)
			throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

		int wordIndex = wordIndex(bitIndex);
		return (wordIndex < words.length) && ((words[wordIndex] & (1L << bitIndex)) != 0);
	}

	public boolean isEmpty() {
		return words.length == 0;
	}

	public int size() {
		return words.length * BITS_PER_WORD;
	}
	
	public String toString() {
		return toString(size());
	}

	public String toString(int numBits) {
		StringBuilder b = new StringBuilder(numBits);
		for (int i = 0; i < numBits; i++)
			b.append(get(i) ? 1 : 0);
		return b.toString();
	}

}
