import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class PointCalculator {

	private final String user = "宇宙";
	private String name;

	public PointCalculator(String name) {
		this.name = name;

	}

	public boolean processLog(int turn, int[] gt_i) throws Exception {

		BufferedReader br = null;
		try {
			// 入力元ファイル
			File file = new File("../SamurAILogger/log/" + name);

			if (file.exists()) {
			} else {
				return false;
			}

			br = new BufferedReader(new FileReader(file));
			String line;
			int ai = 0;
			String aIString = "";
			String[] nameStrings = new String[4];
			int[][] realBox = new int[6][4];
			int[] strength = new int[6];
			while ((line = br.readLine()) != null) {
				// AI番号を取得
				if (ai < 4) {
					nameStrings[ai] = line;
				}
				if (ai < 4) {
					if (line.indexOf(user) != -1) {
						aIString = String.valueOf(ai);
					}
					ai++;
				}

				if (line.indexOf("Turn " + String.valueOf(turn - 1)) != -1) {
					while (line.indexOf("Real Intimacy:") == -1) {
						line = br.readLine();
					}
					for (int i = 0; i < realBox.length; i++) {
						String[] aa = br.readLine().split(" ");
						for (int j = 0; j < realBox[i].length; j++) {
							realBox[i][j] = Integer.parseInt(aa[j]);
						}
					}
				}

				if (line.indexOf("Turn " + String.valueOf(turn)) != -1) {
					while (line.indexOf("Real Intimacy:") == -1) {
						line = br.readLine();
					}
					int me = Integer.parseInt(aIString);
					for (int i = 0; i < realBox.length; i++) {
						String[] aa = br.readLine().split(" ");
						for (int j = 0; j < realBox[i].length; j++) {
							if (j != me) {
								realBox[i][j] = Integer.parseInt(aa[j]);
							}
						}
					}
				}

				if (line.indexOf("Turn finished. Game status:") != -1) {
					line = br.readLine();
					String[] stmp = br.readLine().split(" ");
					for (int i = 0; i < strength.length; i++) {
						strength[i] = Integer.parseInt(stmp[i]);
					}

				}

			}
			// ここで読み終わる
			int me = Integer.parseInt(aIString);
			if (turn % 2 == 0) {
				// 偶数ターン
				for (int i = 3; i < gt_i.length; i++) {
					realBox[gt_i[i]][me] += 2;
				}
			} else {
				for (int i = 0; i < gt_i.length; i++) {
					realBox[gt_i[i]][me]++;
				}
			}

			Lord[] vLords = new Lord[6];
			for (int i = 0; i < vLords.length; i++) {
				vLords[i] = new Lord(strength[i]);
				vLords[i].revealed = new int[4];
				for (int j = 0; j < vLords[i].revealed.length; j++) {
					vLords[i].revealed[j] = realBox[i][j];
				}
			}

			AITools tools = new AITools();
			double[] points = tools.calcPoints(vLords);

			double mine = points[me];
			Arrays.sort(points);
			if (mine == points[3]) {
				return true;
			} else {
				return false;
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// ストリームは必ず finally で close します。
				if (br != null)
					br.close();
			} catch (IOException e) {
			}
		}
		return false;
	}
}