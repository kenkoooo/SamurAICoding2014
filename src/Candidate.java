class Candidate implements Comparable<Candidate> {

	int gt_id;
	double score;

	Candidate(int gt_id, double score) {
		this.gt_id = gt_id;
		this.score = score;
	}

	public int compareTo(Candidate s) {
		double diff = s.score - this.score;
		diff *= 100.0;
		return (int) diff;
	}
}