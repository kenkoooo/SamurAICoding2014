class LordBox implements Comparable<LordBox> {

	int lord_id;
	int strength;

	LordBox(int lord_id, int strength) {
		this.lord_id = lord_id;
		this.strength = strength;
	}

	public int compareTo(LordBox s) {
		return s.strength - this.strength;
	}
}