import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.math.BigDecimal;
import java.math.RoundingMode;

// 簡単な単純ベイズ分類器の実装

public class Naibebayes {

	// データ構造
	// 
	// Map<String, int[2]> biWord
	//   => Map1 の key は word,
	//   => Map1 の value の 1つ目のint は category, 2つ目のint は count
	// int[2] totalWords
	//   => 1つ目のint は カテゴリー0 の 2つ目のintは カテゴリー1 の 文字の総数

	public static Map<String, int[]> biWords = new HashMap<String, int[]>();
	public static int[] totalWords = new int[2];

	public static void main(String[] args) {

		// 1 巨人 2 ヤクルト
		String[] categorize_documents = {
			"1巨人吉川尚輝内野手（27）が“不動の二塁手”としてCS進出へ望みをつなぐ。",
			"1巨人は28日、東京都内でスカウト会議を開き、10月20日のドラフト会議で香川・高松商高の浅野翔吾外野手を1位指名する方針を固めた。",
			"1巨人高梨雄平投手（30）が、若き中継ぎ陣の主体的な成長を求めた。",
			"110月1、2日にDeNAと対戦する巨人・吉川尚輝内野手（27）が28日、攻めの姿勢を貫いてクライマックスシリーズ（CS）切符をつかむと意気込んだ。",
			"1巨人・岡本和真内野手（２６）が２７日、クラマックスシリーズ（ＣＳ）での雪辱を誓った。",
			"0シーズン５６号に王手をかけているヤクルトの村上宗隆内野手（２２）が「４番・三塁」で出場。",
			"0ヤクルトの高津臣吾監督が引退を発表した内川、嶋についてコメントした。",
			"0プロ野球ヤクルトの村上宗隆内野手（22）が、シーズン60本塁打に挑んでいる。",
			"0ヤクルトの村上宗隆内野手（22）は無安打に終わり、日本選手最多を塗り替えるシーズン56本塁打に王手をかけてから11試合ノーアーチとなった。",
			"0ヤクルトは４安打１得点のみで優勝決定後２連敗。",
		};
		//
		totalWords[0] = 0;
		totalWords[1] = 0;
		for (int i = 0; i < categorize_documents.length; i++) {
			learn(categorize_documents[i]);
		}
		//
		// String unknown_document = "56号ホームランがかかるヤクルトの村上宗隆選手が6回、「申告敬遠」で出塁しました。";
		// String unknown_document = "巨人の井上温大投手は２７日、Ｇ球場で行われた１軍の投手練習に参加し、キャッチボールやランニングで汗を流した。";
		// String unknown_document = "日本選手最多５６号に王手をかけているヤクルト・村上は３打数無安打に終わり、新記録はまたもお預けに終わった。";
		String unknown_document = "高梨雄平投手は２７日、Ｇ球場で行われた１軍の投手練習に参加。キャッチボールなどで汗を流した。";
		int judge_result = judge(unknown_document);
		System.out.println(judge_result);
	}

	public static void learn(String text) {
		//
		int category = Integer.parseInt(text.substring(0, 1));
		String learn_text = "";
		learn_text = text.substring(1, text.length());
		learn_text = replace_skip_word(learn_text);
		ArrayList<String> appear = new ArrayList<String>();
		for (int i = 0; i < learn_text.length() - 2; i++) {
			String current_learn_text = learn_text.substring(i, i+3);
			if (appear.contains(current_learn_text)) {
				continue;
			}
			appear.add(current_learn_text);
			int[] biWordsValue;
			if (!biWords.containsKey(current_learn_text)) {
				biWords.put(current_learn_text, new int[2]);
			}
			biWords.get(current_learn_text)[category]++;
			totalWords[category]++;
		}
	}

	public static int judge(String text) {
		String judge_text = replace_skip_word(text);
		ArrayList<String> appear = new ArrayList<String>();
		Set<String> learned_appear_before = biWords.keySet();
		Set<String> learned_appear = new HashSet<String>();
		for (String key : learned_appear_before) {
			learned_appear.add(key);
		}
		for (int i = 0; i < judge_text.length() - 2; i++) {
			String current_judge_text = judge_text.substring(i, i+3);
			learned_appear.add(current_judge_text);
		}
		int category_denominator = learned_appear.size();
		BigDecimal category0_percentage = new BigDecimal(0);
		BigDecimal category1_percentage = new BigDecimal(0);
		for (int j = 0; j < judge_text.length() - 2; j++) {
			String current_judge_text = judge_text.substring(j, j+3);
			if (appear.contains(current_judge_text)) {
				continue;
			}
			appear.add(current_judge_text);
			int category0_numerator = 0;
			int category1_numerator = 0;
			if (biWords.containsKey(current_judge_text)) {
				category0_numerator = biWords.get(current_judge_text)[0];
				category1_numerator = biWords.get(current_judge_text)[1];
			}
			category0_numerator = category0_numerator + 1;
			category1_numerator = category1_numerator + 1;
			BigDecimal category0_numerator_d = new BigDecimal(category0_numerator);
			BigDecimal category1_numerator_d = new BigDecimal(category1_numerator);
			BigDecimal category_denominator_d = new BigDecimal(category_denominator);
			BigDecimal category0_result = category0_numerator_d.divide(category_denominator_d, 5, RoundingMode.HALF_UP);
			BigDecimal category1_result = category1_numerator_d.divide(category_denominator_d, 5, RoundingMode.HALF_UP);
			double category0_result_log = Math.log(category0_result.doubleValue());
			double category1_result_log = Math.log(category1_result.doubleValue());
			BigDecimal category0_result_log_d = new BigDecimal(category0_result_log);
			BigDecimal category1_result_log_d = new BigDecimal(category1_result_log);
			//
			// System.out.println(category0_result);
			// System.out.println(category1_result);
			category0_percentage = category0_percentage.add(category0_result_log_d);
			category1_percentage = category1_percentage.add(category1_result_log_d);
		}
		// System.out.println(category0_percentage);
		// System.out.println(category1_percentage);
		return category0_percentage.compareTo(category1_percentage) == 1 ? 0 : 1;
	}

	public static String replace_skip_word(String text) {
		String replace_text = text;
		replace_text = replace_text.replaceAll("、", "");
		replace_text = replace_text.replaceAll("。", "");
		replace_text = replace_text.replaceAll("（", "");
		replace_text = replace_text.replaceAll("）", "");
		replace_text = replace_text.replaceAll("「", "");
		replace_text = replace_text.replaceAll("」", "");
		replace_text = replace_text.replaceAll(",", "");
		replace_text = replace_text.replaceAll("\\.", "");
		replace_text = replace_text.replaceAll("\"", "");
		replace_text = replace_text.replaceAll("”", "");
		replace_text = replace_text.replaceAll("“", "");
		replace_text = replace_text.replaceAll("'", "");
		// and more ...
		return replace_text;
	}
}