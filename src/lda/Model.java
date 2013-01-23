/**
 * 
 */
package lda;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author pgadde Mostly taken from JGibbsLDA. Works with my Dataset and Vocab
 *         classes
 */
public class Model {

	public Dataset data; // the dataset
	public int M; // number of documents
	public int V; // number of words
	public int K; // number of topics
	public double alpha, beta; // hyper-parameters
	public int niters; // number of iterations
	private int liter;

	// Estimated parameters
	public double[][] theta; // document-topic distributions, size M x K
	public double[][] phi; // topic-word distributions, size K x V

	// Temporary variables used while sampling
	public ArrayList<Integer>[] z; // topic assignments for words
	protected int[][] nw; // i,j: word i instances assigned to topic j
	protected int[][] nd; // i,j: words in document i assigned to topic j
	protected int[] nwsum; // j: total number of words assigned to topic j
	protected int[] ndsum; // i: total number of words in document i
	protected double[] p; // some temporary variables of the size of topics

	public Model() {
		setDefaultValues();
	}

	private void setDefaultValues() {
		K = 100;
		alpha = 50.0 / K;
		beta = 0.1;
		niters = 2000;
		z = null;
		nw = null;
		nd = null;
		nwsum = null;
		ndsum = null;
		theta = null;
		phi = null;
		liter = 0;
	}

	public void setData(Dataset data) {
		this.data = data;
	}

	public void setK(int k) {
		K = k;
	}

	public void setNiters(int niters) {
		this.niters = niters;
	}

	@SuppressWarnings("unchecked")
	public void initializeModel() {
		int m, n, w, k;
		p = new double[K];
		M = data.M;
		V = data.V;
		nw = new int[V][K];
		for (w = 0; w < V; w++) {
			for (k = 0; k < K; k++) {
				nw[w][k] = 0;
			}
		}

		nd = new int[M][K];
		for (m = 0; m < M; m++) {
			for (k = 0; k < K; k++) {
				nd[m][k] = 0;
			}
		}

		nwsum = new int[K];
		for (k = 0; k < K; k++) {
			nwsum[k] = 0;
		}

		ndsum = new int[M];
		for (m = 0; m < M; m++) {
			ndsum[m] = 0;
		}

		z = new ArrayList[M];
		for (m = 0; m < data.M; m++) {
			int N = data.docs.get(m).totalWords();
			z[m] = new ArrayList<Integer>();

			// initialize for z
			for (n = 0; n < N; n++) {
				int topic = (int) Math.floor(Math.random() * K);
				z[m].add(topic);

				// number of instances of word assigned to topic j
				// System.out.println(m+" "+n+" "+K+" "+data.docs.get(m).words.get(n)+" "+data.vocab.id(data.docs.get(m).words.get(n))+" "+V);
				nw[data.get(m, n)][topic] += 1;
				// number of words in document i assigned to topic j
				nd[m][topic] += 1;
				// total number of words assigned to topic j
				nwsum[topic] += 1;
			}
			// total number of words in document i
			ndsum[m] = N;
		}

		theta = new double[M][K];
		phi = new double[K][V];
	}

	public void estimate() {
		System.out.println("Sampling " + niters + " iteration!");

		int lastIter = liter;
		for (liter = lastIter + 1; liter < niters + lastIter; liter++) {
			System.out.println("Iteration " + liter + " ...");

			// for all z_i
			for (int m = 0; m < M; m++) {
				for (int n = 0; n < data.docs.get(m).totalWords(); n++) {
					// z_i = z[m][n]
					// sample from p(z_i|z_-i, w)
					int topic = sampling(m, n);
					z[m].set(n, topic);
				}// end for each word
			}// end for each document
		}// end iterations

		System.out.println("Gibbs sampling completed!\n");
		System.out.println("Saving the final model!\n");
		computeTheta();
		computePhi();
		liter--;
		// saveModel("model-final");
	}

	/**
	 * Do sampling
	 * 
	 * @param m
	 *            document number
	 * @param n
	 *            word number
	 * @return topic id
	 */
	public int sampling(int m, int n) {
		// remove z_i from the count variable
		int topic = z[m].get(n);
		int w = data.get(m, n);

		nw[w][topic] -= 1;
		nd[m][topic] -= 1;
		nwsum[topic] -= 1;
		ndsum[m] -= 1;

		double Vbeta = V * beta;
		double Kalpha = K * alpha;

		// do multinominal sampling via cumulative method
		for (int k = 0; k < K; k++) {
			p[k] = (nw[w][k] + beta) / (nwsum[k] + Vbeta) * (nd[m][k] + alpha)
					/ (ndsum[m] + Kalpha);
		}

		// cumulate multinomial parameters
		for (int k = 1; k < K; k++) {
			p[k] += p[k - 1];
		}

		// scaled sample because of unnormalized p[]
		double u = Math.random() * p[K - 1];

		for (topic = 0; topic < K; topic++) {
			if (p[topic] > u) // sample topic w.r.t distribution p
				break;
		}

		// add newly estimated z_i to count variables
		nw[w][topic] += 1;
		nd[m][topic] += 1;
		nwsum[topic] += 1;
		ndsum[m] += 1;

		return topic;
	}

	public void computeTheta() {
		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				theta[m][k] = (nd[m][k] + alpha) / (ndsum[m] + K * alpha);
			}
		}
	}

	public void computePhi() {
		for (int k = 0; k < K; k++) {
			for (int w = 0; w < V; w++) {
				phi[k][w] = (nw[w][k] + beta) / (nwsum[k] + V * beta);
			}
		}
	}

	public boolean saveModelTwords(String filename, int twords) {
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filename), "UTF-8"));

			for (int k = 0; k < K; k++) {
				List<Pair> wordsProbsList = new ArrayList<Pair>();
				for (int w = 0; w < V; w++) {
					Pair p = new Pair(w, phi[k][w], false);

					wordsProbsList.add(p);
				}// end foreach word

				// print topic
				writer.write("Topic " + k + "th:\n");
				Collections.sort(wordsProbsList);

				for (int i = 0; i < twords; i++) {
					if (data.vocab.vocab.inverse().containsKey(
							(Integer) wordsProbsList.get(i).first)) {
						String word = data.vocab.word((Integer) wordsProbsList
								.get(i).first);

						writer.write("\t" + word + " "
								+ wordsProbsList.get(i).second + "\n");
					}
				}
			} // end foreach topic

			writer.close();
		} catch (Exception e) {
			System.out.println("Error while saving model twords: "
					+ e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
