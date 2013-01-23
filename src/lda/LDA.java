package lda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author pgadde
 * 
 */
public class LDA {

	Model ldaModel;
	Dataset data;

	public LDA() {
		ldaModel = null;
		data = null;
	}

	/**
	 * A public method that calls the dataset loading private method, as of now
	 * Having these two functions as I see a possibility of doing some
	 * pre-processing after loading the dataset, that can be written in another
	 * method and called from the public function
	 * 
	 * @param docFile
	 * @throws IOException
	 */
	public void loadDataset(String docFile) throws IOException {
		prepareDataset(docFile);
	}

	private void prepareDataset(String docFile) throws IOException {
		System.out.println("Preparing the dataset");
		data = new Dataset();
		Vocabulary V = new Vocabulary();
		ArrayList<Document> docs = new ArrayList<Document>();
		V.loadVocab(docFile); // Reading the entire dataset once. Helps doing
		// word pre-processing later
		data.setVocab(V);
		BufferedReader reader = new BufferedReader(new FileReader(docFile));
		String line = null;
		int lineCount = 0;
		while ((line = reader.readLine()) != null) {
			List<String> words = Arrays.asList(line.split("\\s"));
			ArrayList<Integer> wordIds = new ArrayList<Integer>();
			for (String word : words) {
				if (V.vocab.containsKey(word))
					wordIds.add(V.id(word));
			}
			Document D = new Document();
			D.loadDocument(wordIds);
			docs.add(D);
			lineCount++;
			if (lineCount % 10000 == 0)
				System.err.print(lineCount + " ");
		}
		System.out.println();
		data.setV(V.size());
		data.setDocs(docs);
		data.setM(docs.size());
		System.out.println("Dataset prepared");
	}

	public void initializeModel(int k, int iters) {
		System.out.println("Initializing the model");
		ldaModel = new Model();
		ldaModel.setData(data);
		ldaModel.setK(k);
		ldaModel.setNiters(iters);
		ldaModel.initializeModel();
		System.out.println("Model initialized");
	}

	public void estimate(String topWordsFile) {
		System.out.println("Estimating the model");
		ldaModel.estimate();
		System.out.println("Printing top 50 words");
		ldaModel.saveModelTwords(topWordsFile, 50);
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		// Assuming training options for now
		// String docFile =
		// "/usr0/home/pgadde/Work/Ethnic/Hoodup/DataExploration/SampledPosts2/TopicChange/MyLDA/posts";
		String docFile = args[0];
		LDA L = new LDA();
		L.loadDataset(docFile);
		int topics = Integer.parseInt(args[1]);
		int iters = Integer.parseInt(args[2]);
		L.initializeModel(topics, iters);
		// String topWordsFile =
		// "/usr0/home/pgadde/Work/Ethnic/Hoodup/DataExploration/SampledPosts2/TopicChange/MyLDA/topWords";
		String topWordsFile = args[3];
		L.estimate(topWordsFile);
	}
}
