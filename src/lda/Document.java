package lda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author pgadde
 * 
 */
public class Document {

	HashMap<Integer, Double> topicScores; // Currently not using updating topic
											// scores
	ArrayList<Integer> words;
	int totalWords = 0;

	public Document() {
		words = new ArrayList<Integer>();
		topicScores = new HashMap<Integer, Double>();
	}

	public void loadDocument(ArrayList<Integer> words) {
		this.words = words;
		totalWords += words.size();
	}

	public int totalWords() {
		return totalWords;
	}

	public int get(int n) {
		return words.get(n);
	}

	public double getTopicScore(int index) {
		return topicScores.get(index);
	}

	public List<Double> getTopicScores() {
		// Not sure about the cast
		return (List<Double>) topicScores.values();
	}
}
