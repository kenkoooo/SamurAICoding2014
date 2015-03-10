import java.util.ArrayList;
import java.util.Collections;

public class AITools {

	public void evaLog(int gt_id, double[] eval, int[][] gt) {
		for (int j = 0; j < gt[gt_id].length; j++) {
			System.out.print(gt[gt_id][j] + " ");
		}
		for (int j = 0; j < eval.length; j++) {
			System.out.print(eval[j] + " ");
		}
		System.out.print("\n");
	}

	public void evaLog(int gt_id, double eval, int[][] gt) {
		for (int j = 0; j < gt[gt_id].length; j++) {
			System.out.print(gt[gt_id][j] + " ");
		}
		System.out.print(eval + " ");
		System.out.print("\n");
	}

	public int[][] makeGT() {
		int[][] gt = new int[252][5];
		int i = 0;
		for (int j1 = 0; j1 < 6; j1++) {
			for (int j2 = j1; j2 < 6; j2++) {
				for (int j3 = j2; j3 < 6; j3++) {
					for (int j4 = j3; j4 < 6; j4++) {
						for (int j5 = j4; j5 < 6; j5++) {
							gt[i][0] = j1;
							gt[i][1] = j2;
							gt[i][2] = j3;
							gt[i][3] = j4;
							gt[i][4] = j5;
							i++;
						}
					}
				}
			}
		}
		return gt;
	}

	public int[] sortMap(ArrayList<Candidate> list, int lim) {
		Collections.sort(list);

		// 内容を表示
		int tmp = 0;
		int[] array = new int[lim];
		for (Candidate candidate : list) {
			array[tmp] = candidate.gt_id;
			tmp++;
			if (tmp == lim) {
				break;
			}
		}

		return array;
	}

	public double[] calcPoints(Lord[] vLords) {
		double[] zenhan = new double[4];
		return calcPoints(vLords, zenhan);
	}

	public double[] calcPoints(Lord[] vLords, double[] zenhan) {
		// とりあえずrevealだけ見て計算する

		double[] points = new double[4];
		for (int i = 0; i < points.length; i++) {
			// 前半分をいれる
			points[i] += zenhan[i];
		}

		for (int i = 0; i < vLords.length; i++) {
			int max = 0;
			int min = Integer.MAX_VALUE;
			int max_num = 0;
			int min_num = 0;
			for (int j = 0; j < vLords[i].revealed.length; j++) {
				int num = vLords[i].revealed[j];
				if (num > max) {
					max = num;
					max_num = 1;
				} else if (num == max) {
					max_num++;
				}

				if (num < min) {
					min = num;
					min_num = 1;
				} else if (num == min) {
					min_num++;
				}
			}

			if (max == min) {
				continue;
			}

			for (int j = 0; j < vLords[i].revealed.length; j++) {
				if (vLords[i].revealed[j] == max) {
					points[j] += (double) vLords[i].strength / max_num;
				} else if (vLords[i].revealed[j] == min) {
					points[j] -= (double) vLords[i].strength / min_num;
				}
			}
		}

		return points;
	}

	public Lord[] lordClone(Lord[] originalLords) {
		int lordsNumber = originalLords.length;
		Lord[] vLords = new Lord[lordsNumber];
		for (int i = 0; i < lordsNumber; i++) {
			int militaryStrength = originalLords[i].strength;
			vLords[i] = new Lord(militaryStrength);
			vLords[i].revealed = originalLords[i].revealed.clone();
			for (int j = 0; j < vLords.length; j++) {
				vLords[i].revealed[0] = originalLords[i].real;
			}
			vLords[i].hidden = originalLords[i].hidden;
			vLords[i].real = originalLords[i].real;
		}
		return vLords;
	}

}