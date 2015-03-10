import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class SampleAI {
	// もともと入ってたやつ
	private static Random random;
	private static int maxTurn, turn, playersNum, lordsNum;
	private static Lord[] lords;
	private static Scanner scanner;
	private static PrintWriter writer;

	// 自分で入れたやつ
	private static int[][] gt;// 合法手列挙
	private static int[][] hidden_record;// 非公開情報記録用
	private static int[][] my_hidden_record;// 自分の非公開情報記録用
	private static int[][] cases;
	private static AITools tools;
	private static double[] zenhan;// 前半戦の得点
	private static int enemy_lim = 252;// 敵の予想手数

	public static void main(String[] args) {
		random = new Random();
		tools = new AITools();
		scanner = new Scanner(System.in);
		writer = new PrintWriter(System.out, true);
		gt = tools.makeGT();
		hidden_record = new int[4][6];
		my_hidden_record = new int[4][2];
		cases = new int[4][252];
		zenhan = new double[4];
		writer.println("READY");
		writer.flush();
		readInitialData();
		for (int t = 0; t < maxTurn; t++) {
			readData();
			writeCommand();
		}
		scanner.close();
		writer.close();
	}

	private static void readInitialData() {
		maxTurn = scanner.nextInt();
		playersNum = scanner.nextInt();
		lordsNum = scanner.nextInt();
		lords = new Lord[lordsNum];
		for (int i = 0; i < lordsNum; i++) {
			int militaryStrength = scanner.nextInt();
			lords[i] = new Lord(militaryStrength);
		}
	}

	private static void readData() {
		turn = scanner.nextInt();
		char time = scanner.next().charAt(0);
		for (int i = 0; i < lordsNum; i++) {
			int[] revealedIntimacy = new int[playersNum];
			for (int j = 0; j < playersNum; j++) {
				revealedIntimacy[j] = scanner.nextInt();
			}
			lords[i].revealed = revealedIntimacy;
		}
		for (int i = 0; i < lordsNum; i++) {
			int realIntimacy = scanner.nextInt();
			lords[i].real = realIntimacy;
		}
		if (time == 'D') {
			for (int i = 0; i < lordsNum; i++) {
				int negotiationCount = scanner.nextInt();
				lords[i].hidden = negotiationCount;

			}

			if (turn >= 2) {
				int now = (turn - 3) / 2;
				int k = 0;
				for (int i = 0; i < lords.length; i++) {
					int real = lords[i].real;
					int revealed = lords[i].revealed[0];
					int hidden = real - revealed;
					hidden /= 2;
					if (turn % 4 == 1) {
						int a = 0;
						if (turn == 9) {
							a = 2;
						}
						if (my_hidden_record[a][0] == i) {
							hidden--;
						}
						if (my_hidden_record[a][1] == i) {
							hidden--;
						}
					}

					for (int j = 0; j < hidden; j++) {
						my_hidden_record[now][k] = i;
						k++;
					}
				}
			}
			refreshHiddenRecord();
		}

		// 前半の得点計算
		if (turn == 6) {
			Lord[] vLords = tools.lordClone(lords);
			double[] points = tools.calcPoints(vLords);
			for (int i = 0; i < points.length; i++) {
				zenhan[i] = points[i];
			}
		}
	}

	private static void refreshHiddenRecord() {
		// refreshが必要なのはターン3,5,7,9
		if (turn % 2 == 0 || turn == 1) {
			return;
		}

		for (int i = 0; i < lords.length; i++) {
			hidden_record[(turn - 3) / 2][i] = lords[i].hidden;
		}
		for (int j = 0; j < my_hidden_record[(turn - 3) / 2].length; j++) {
			int choice = my_hidden_record[(turn - 3) / 2][j];
			hidden_record[(turn - 3) / 2][choice]--;
		}

	}

	private static void writeCommand() {
		StringBuilder command = new StringBuilder();
		int key = monster();
		for (int i = 0; i < gt[key].length; i++) {
			if (turn % 2 == 0 && i == 0) {
				i = 3;
			}
			command.append(gt[key][i]);
			command.append(" ");
		}
		writer.println(command.toString());
		writer.flush();
	}

	public static Lord[] makeVLords(int player_k, int gt_id) {
		// プレイヤーkが合法手gt_iを入れた時の
		// ランダムプレイしてみる
		// 引数の合法手でvLordsを作ってランダムプレイさせて返す

		// clone闇が深い
		Lord[] vLords = tools.lordClone(lords);

		// 非公開情報をランダムでいれる
		int start = turn >= 6 ? 2 : 0;
		int stop = (turn - 1) / 2;
		for (int i = start; i < stop; i++) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			for (int j = 0; j < hidden_record[i].length; j++) {
				for (int j2 = 0; j2 < hidden_record[i][j]; j2++) {
					list.add(j);
				}
			}
			Collections.shuffle(list);
			for (int j = 0; j < list.size(); j++) {
				// 01:1,23:2,45:3
				vLords[list.get(j)].revealed[(j + 2) / 2] += 2;
			}
		}

		// 自分の合法手情報をいれる
		int plus = turn % 2 == 0 ? 2 : 1;
		for (int i = 0; i < gt[gt_id].length; i++) {
			if (turn % 2 == 0 && i == 0) {
				i = 3;
			}
			vLords[gt[gt_id][i]].revealed[player_k] += plus;
		}

		if (turn % 2 == 1) {
			// 奇数ターンはランダムで敵の情報を入れる
			int enemy_rand = random.nextInt(enemy_lim);
			if (player_k != 0) {
				enemy_rand = random.nextInt(252);
			}

			for (int player_id = 0; player_id < playersNum; player_id++) {
				if (player_id != player_k) {
					int enemy_gt = cases[player_id][enemy_rand];
					for (int j = 0; j < gt[gt_id].length; j++) {
						vLords[gt[enemy_gt][j]].revealed[player_id]++;
					}
				}
			}

		} else {
			// 偶数ターンは全探索やめた
			int enemy_rand = random.nextInt(enemy_lim);
			if (player_k != 0) {
				enemy_rand = random.nextInt(21);
			}

			for (int player_id = 0; player_id < playersNum; player_id++) {
				if (player_id != player_k) {
					int enemy_gt = cases[player_id][enemy_rand];
					for (int j = 3; j < gt[gt_id].length; j++) {
						vLords[gt[enemy_gt][j]].revealed[player_id]++;
					}
				}
			}
		}

		// ランダムプレイアウト
		int sim_turn = turn + 1;
		while ((turn <= 5 && sim_turn <= 5) || (turn > 5 && sim_turn <= 9)) {
			if (sim_turn % 2 == 1) {
				// 奇数ターンのシミュレーション
				for (int vPlayer = 0; vPlayer < playersNum; vPlayer++) {
					int rand_gt_id = random.nextInt(252);
					for (int j = 0; j < gt[rand_gt_id].length; j++) {
						vLords[gt[rand_gt_id][j]].revealed[vPlayer]++;
					}
				}
			} else {
				// 偶数ターンのシミュレーション
				for (int vPlayer = 0; vPlayer < playersNum; vPlayer++) {
					int rand_gt_id = random.nextInt(21);
					for (int j = 3; j < gt[rand_gt_id].length; j++) {
						vLords[gt[rand_gt_id][j]].revealed[vPlayer] += 2;
					}
				}
			}
			sim_turn++;
		}
		// ランダムプレイアウトおわり

		return vLords;
	}

	private static double[] evaluateHand(int gt_id, int player_id, int loop, boolean wins) {
		double[] eval = new double[4];

		for (int k = 0; k < loop; k++) {
			Lord[] vLords = makeVLords(player_id, gt_id);// ランダムプレイ後の棋譜を受け取る

			double[] p = tools.calcPoints(vLords, zenhan);// ポイントを計算する

			if (wins) {
				// 勝利数で評価する
				ArrayList<Candidate> list = new ArrayList<Candidate>();
				for (int i = 0; i < p.length; i++) {
					list.add(new Candidate(i, p[i] + 0.001 * (3 - i)));
				}
				Collections.sort(list);

				int winner = list.get(0).gt_id;
				eval[winner] += 1.0;
				// if (turn % 4 != 1) {
				// // 最終ターン以外は2位には1/4の得点
				// int first_runner_up = list.get(1).gt_id;
				// eval[first_runner_up] += 0.25;
				// }

			} else {
				// 点数で評価する
				for (int j = 0; j < p.length; j++) {
					eval[j] += p[j];
				}
			}

		}

		return eval;
	}

	private static int[] extractAllowed() {
		int lim_forbidden = 2;
		if (turn == 1 || turn == 6) {
			lim_forbidden = 4;
		} else if (turn == 2 || turn == 7) {
			lim_forbidden = 3;
		}

		// 対応表を作る
		int[] lordBox = new int[6];
		ArrayList<LordBox> boxs = new ArrayList<LordBox>();
		for (int i = 0; i < lords.length; i++) {
			int max = 0;
			for (int j = 0; j < lords[i].revealed.length; j++) {
				max = Math.max(max, lords[i].revealed[j]);
			}
			max *= 300;
			if (turn < 6) {
				max += (hidden_record[0][i] + hidden_record[1][i]) * 200;
			}
			if (turn >= 6) {
				max += (hidden_record[2][i] + hidden_record[3][i]) * 200;
			}
			boxs.add(new LordBox(i, max + lords[i].strength * 10 + random.nextInt(10)));
		}
		Collections.sort(boxs);
		for (int i = 0; i < lordBox.length; i++) {
			lordBox[i] = boxs.get(i).lord_id;
		}

		// 禁じ手システム
		ArrayList<Integer> list = new ArrayList<Integer>();
		int all = 252;
		if (turn % 2 == 0) {
			all = 21;
		}
		for (int i = 0; i < all; i++) {
			boolean add = true;
			for (int j = 0; j < gt[i].length; j++) {
				if (j == 0 && turn % 2 == 0) {
					j = 3;
				}
				for (int k = 0; k < lim_forbidden; k++) {
					if (lordBox[k] == gt[i][j]) {
						add = false;
						break;
					}
				}
				if (!add) {
					break;
				}
			}
			if (add) {
				list.add(i);
			}
		}
		int[] tmp = new int[list.size()];
		for (int i = 0; i < tmp.length; i++) {
			tmp[i] = list.get(i);
		}

		return tmp;
	}

	private static int[] extractMap(int player_id, int prev_lim, int next_lim, int loop) {

		// 生成
		ArrayList<Candidate> list = new ArrayList<Candidate>();
		for (int i = 0; i < prev_lim; i++) {
			// casesから取り出します
			int gt_id = cases[player_id][i];

			double[] eval = evaluateHand(gt_id, player_id, loop, true);
			list.add(new Candidate(gt_id, eval[player_id]));

		}

		return tools.sortMap(list, next_lim);
	}

	private static int monster() {
		if (turn % 2 == 1) {
			int lim0 = 252;// 最初の候補手リスト数
			enemy_lim = 252;
			int loop0 = 100;// 最初のループ回数//このパラメータは強さにあまり関係ない
			int lim1 = 100;// 二回目の候補手数(自分)
			int loop1 = 2000;// 二回目のループ回数
			int lim2 = 10;// 三回目の候補手数

			// まずは全通り列挙
			for (int i = 0; i < lim0; i++) {
				for (int j = 0; j < playersNum; j++) {
					cases[j][i] = i;
				}
			}

			// 敵の手を絞り込み
			if (turn == 5 || turn == 9) {
				enemy_lim = 70;
				for (int vPlayer = 1; vPlayer < playersNum; vPlayer++) {
					int[] enemy = extractMap(vPlayer, lim0, enemy_lim, loop0);
					for (int i = 0; i < enemy.length; i++) {
						cases[vPlayer][i] = enemy[i];
					}
				}
			}

			if (turn == 1 || turn == 3 || turn == 7) {
				// 禁じ手を入れてみる
				int[] list1 = extractAllowed();
				for (int i = 0; i < list1.length; i++) {
					cases[0][i] = list1[i];
				}
				lim1 = list1.length;
			} else {
				// lim0個→lim1個に絞る
				int[] list1 = extractMap(0, lim0, lim1, loop0);
				for (int i = 0; i < list1.length; i++) {
					cases[0][i] = list1[i];
				}
			}

			// lim1個→lim2個に絞る
			int[] list2 = extractMap(0, lim1, lim2, loop1);
			for (int i = 0; i < list2.length; i++) {
				cases[0][i] = list2[i];
			}

			// // TODO
			// for (int i = 0; i < list2.length; i++) {
			// PointCalculator calculator = new PointCalculator("宇宙-114073");
			// try {
			// System.out.println(calculator.processLog(turn, gt[list2[i]]) +
			// " ");
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }

			return list2[0];
		} else {
			// 偶数ターン用設定
			int lim0 = 21;// 最初の候補手リスト数
			enemy_lim = 21;
			int loop0 = 10000;// 最初のループ回数//このパラメータは強さにあまり関係ない

			// まずは全通り列挙
			for (int i = 0; i < lim0; i++) {
				for (int j = 0; j < playersNum; j++) {
					cases[j][i] = i;
				}
			}

			// 禁じてテスト
			if (turn == 2 || turn == 6) {
				int[] test = extractAllowed();
				for (int i = 0; i < test.length; i++) {
					cases[0][i] = test[i];
				}
				lim0 = test.length;
			}

			// lim0個→lim1個に絞る
			int[] list1 = extractMap(0, lim0, 1, loop0);
			for (int i = 0; i < list1.length; i++) {
				cases[0][i] = list1[i];
			}
			return list1[0];

		}

	}

}
