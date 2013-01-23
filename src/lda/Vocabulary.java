/**
 * 
 */
package lda;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

/**
 * @author pgadde
 * 
 */
public class Vocabulary {

	// Guava's Multisets and Bimaps are cool for maintaining bags of words!
	BiMap<String, Integer> vocab;
	Multiset<String> wordDocCounts;
	Multiset<String> wordCounts;
	int minOccurance;
	int minDocOccurance;

	public Vocabulary() {
		vocab = HashBiMap.create();
		wordCounts = HashMultiset.create();
		wordDocCounts = HashMultiset.create();
		setDefaults();
	}

	private void setDefaults() {
		minOccurance = 10;
		minDocOccurance = 50;
	}

	public void loadVocab(String dataFile) throws IOException {
		System.out.println("Reading the file to load the vocab");
		int numLines = 0;
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = null;
		while ((line = reader.readLine()) != null) {
			List<String> words = Arrays.asList(line.split("\\s"));
			wordCounts.addAll(words);
			wordDocCounts.addAll(new HashSet<String>(words));
			numLines += 1;
			if (numLines % 10000 == 0)
				System.err.println(numLines + " ");
		}
		System.out.println();
		preProcess();
		System.out.println("Done loading the vocab");
	}

	/**
	 * Removes words which don't occur in at least minDocOccurance documents
	 * Removes words which doen't occur at least minOccurance times
	 */
	public void preProcess() {
		int id = 0;
		for (String word : wordCounts.elementSet()) {
			if (wordCounts.count(word) >= minOccurance
					&& wordDocCounts.count(word) >= minDocOccurance) {
				vocab.forcePut(word, id);
				id++;
			}
		}
		wordCounts.clear();
		wordDocCounts.clear();
		System.out.println("Vocabulary size:" + vocab.size());
	}

	/**
	 * @param word
	 * @return the word's id
	 */
	public int id(String word) {
		return vocab.get(word);
	}

	/**
	 * Bimap comes handy!
	 * 
	 * @param id
	 * @return the words with ID id
	 */
	public String word(int id) {
		return vocab.inverse().get(id);
	}

	public int size() {
		return vocab.size();
	}

}
