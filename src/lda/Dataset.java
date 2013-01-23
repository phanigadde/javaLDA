package lda;

import java.util.ArrayList;

/**
 * @author pgadde Just a convenient group of my vocab and documents
 */
public class Dataset {

	public Vocabulary vocab; // local dictionary
	public ArrayList<Document> docs; // a list of documents
	public int M; // number of documents
	public int V; // number of words

	public void setVocab(Vocabulary vocab) {
		this.vocab = vocab;
	}

	public void setDocs(ArrayList<Document> docs) {
		this.docs = docs;
	}

	public void setM(int m) {
		M = m;
	}

	public void setV(int v) {
		V = v;
	}

	public int get(int m, int n) {
		return docs.get(m).get(n);
	}

}
