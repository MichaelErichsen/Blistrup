package net.myerichsen.archivesearcher.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class encodes a Danish name phonetically
 *
 * @author Michael Erichsen
 * @version 6. jul. 2023
 */
public class Fonkod {

	/**
	 * No-arg c:tor
	 *
	 */
	public Fonkod() {
	}

	/**
	 * Den del af navnet, der ligger foran første vokalgruppe, undersøges for
	 * umulige konsonantsammenstillinger.
	 *
	 * @param textField
	 * @return String
	 */
	private String eliminateImpossibleConsonantCombinations(String input) {
		String out = input;
		if (out.length() < 2 || isVowel(out.charAt(0), out.charAt(1))) {
			return out;
		}

		// Er der overhovedet vokaler?
		Pattern p = Pattern.compile("[aeiouyæøå]+");
		Matcher m = p.matcher(out);
		if (!m.find()) {
			return out;
		}

		// æøå etc
//		out = out.replace("Ã¸", "ø").replace("Ã¥", "å").replace("Ã«", "ë").replace("Ã¼", "ü");
		out = out.replace("Ã¸", "ø").replace("Ã¥", "å").replace("Ã«", "e").replace("Ã¼", "u");

		// _n/l/r jC -> _n/l/r j
		out = out.replaceFirst("\\b([nlr]j)[^aeiouyæøå]", "$1");

		// _n/l/r (C^j) -> _n/l/r
		out = out.replaceFirst("\\b([nlr])[^aeijouyæøå]", "$1");

		// _j -> _
		out = out.replaceFirst("\\bj", "");

		// _^(s/n/l/r/j) n/l/r/v/j j -> _^(s/n/l/r/j) n/l/r/v/j
		out = out.replaceFirst("\\b([^snlrj][nlrvj])j", "$1");

		// _^(s/n/l/r/j) n/l/r/v/j (C^j) -> _^(s/n/l/r/j) (C^j)
		out = out.replaceFirst("\\b([^snlrj])[nlrvj]([^aeijouyæøå])", "$1$2");

		// st j/r C -> st j/r
		out = out.replaceFirst("(st[jr])[^aeiouyæøå]", "$1");

		// st (C^j/r) -> st
		out = out.replaceFirst("(st)[^aeijoruyæøå]", "$1");

		// sp j/r/l C -> st j/r/l
		out = out.replaceFirst("(sp[jrl])[^aeiouyæøå]", "$1");

		// sp (C^j/r/l) -> sp
		out = out.replaceFirst("(sp)[^aeijloruyæøå]", "$1");

		// skr -> sr
		out = out.replaceFirst("skr", "sr");

		// skj -> sk & sj
		p = Pattern.compile("skj");

		if (out.length() >= 3) {
			m = p.matcher(out.substring(0, 3));
			if (m.matches()) {
				return out.replaceFirst("skj", "sk") + " " + out.replaceFirst("skj", "sj");
			}
		}

		return out.replaceFirst("(sk)[^aeijoruyæøå]", "$1");
	}

	/**
	 * Navnedelen efter første vokal behandles samlet
	 *
	 * @param input
	 * @return String
	 */
	private String finalizeProcessing(String input) {
		String out = input;
		Pattern p;
		Matcher m;

		// Navnedel efter første vokalgruppe
		// Hvis navn slutter med første vokalgruppe, returneres
		p = Pattern.compile("[aeiouyæøå]+[^aeiouyæøå]");
		m = p.matcher(input);
		if (!m.find()) {
			return out;
		}

		// sC_
		out = out.replaceFirst("([aeiouyæøå]+[^aeiouyæøå]*s[^aieouyæøå])\\b", "$1");

		// sC...s/V
		out = out.replaceFirst("([aeiouyæøå]+[^aeiouyæøå]*s[^aieouyæøå])[^aeiosuyæøå]*([aeiosuyæøå]).*", "$1$2");

		// sV -> sr
		out = out.replaceFirst("([aeiouyæøå]+[^aeiouyæøå]*+s)[aieouyæøå].*", "$1r");

		// (C^s)s
		out = out.replaceFirst("([aeiouyæøå]+[^aeiouyæøå]*[^aieosuyæøå]s).*", "$1");

		// (C^s)(C^s)...s/V
		out = out.replaceFirst("([aeiouyæøå]+[^aeiouyæøå]*[^aieosuyæøå]{2})[^aeiosuyæøå]*([aeiosuyæøå]).*", "$1$2");

		// (C^s)(C^s)_
		out = out.replaceFirst("([aeiouyæøå]+[^aeiouyæøå]*[^aieosuyæøå]{2})\\b", "$1");

		return out.replaceFirst("([aeiouyæøå]+[^aeiouyæøå]*[^aieosuyæøå])[^aeiosuyæøå]*([aeiosuyæøå]).*", "$1$2");
	}

	/**
	 * Transform a family name to a Danish phonetic encoding
	 *
	 * @param slaegtsNavn a family name
	 * @return the phonetic representation of the family name
	 * @throws Exception If name missing or not valid
	 */
	public String generateKey(String slaegtsNavn) throws Exception {
		if (slaegtsNavn == null) {
			throw new Exception("Slægtsnavn mangler");
		}

		String out = slaegtsNavn.toLowerCase();
		String[] sa;

		// Kun bogstaver, blank, bindestreg og gnyf er tilladt
		out = out.replace("'", "");
		Pattern p = Pattern.compile("([a-zæøåäçéëöüÿž]|/|-|\\.|\\s)+");
		Matcher m = p.matcher(out);
		if (!m.matches()) {
			// throw new Exception("Ugyldigt slægtsnavn: " + slaegtsNavn);
			return "";
		}

		if (out.length() == 1) {
			return out.trim();
		}

		/**
		 * Det givne navn sammenlignes med en tabel over almindelige navne. Hvis navnet
		 * findes heri, benyttes den hertil svarende tabelkode
		 *
		 * Rykket foran næste afsnit af MER
		 *
		 */
		// st = new StringTokenizer(out, " ");
		// sb = new StringBuffer();
		// NameTable nt = new NameTable();
		// String s = "";
		// while (st.hasMoreTokens()) {
		// s = nt.getTableCode(sa[i]);
		// if (s.trim().length() > 0) {
		// System.out.println(s + "\t found in lookUpTable ");
		// return s;
		// }
		// }
		// System.out.println(out + "\tNot found in lookUpTable ");
		/**
		 * Indledningsvis omsættes visse tegn, således at en første standardisering af
		 * navnet opnås
		 */
		p = Pattern.compile("(\\.|-|\\s|/)+");
		sa = p.split(out);
		StringBuilder sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(performInitialStandardization(element) + " ");
			// System.out.println(sb.toString() +
			// "\tperformInitialStandardization ");
		}

		/**
		 * Første vokalgruppe identificeres og omdannes efter behov. Hvis flere end to
		 * vokaler indgår i en vokalgruppe, behandles vokalerne parvis med start forfra
		 * i gruppen.
		 */
		sa = p.split(sb.toString().trim());
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(processFirstVowelGroup(element) + " ");
			// System.out.println(sb.toString() + "\tprocessFirstVowelGroup");
		}

		/**
		 * Efter behandlingen af første vokalgruppe behandles evt. foranstillede
		 * konsonanter sammen med første vokal
		 */
		sa = p.split(sb.toString());
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(processFirstConsonantGroup(element) + " ");
			// System.out.println(sb.toString() +
			// "\tprocessFirstConsonantGroup");
		}

		/**
		 * En evt. anden vokalgruppe behandles som den første
		 */
		sa = p.split(sb.toString());
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(processSecondVowelGroup(element) + " ");
			// System.out.println(sb.toString() + "\tprocessSecondVowelGroup");
		}

		/**
		 * Navnedelen efter første vokalgruppe behandles
		 */
		sa = p.split(sb.toString());
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(processSecondConsonantGroup(element) + " ");
			// System.out.println(sb.toString() +
			// "\tprocessSecondConsonantGroup");
		}

		/**
		 * De to vokalgrupper samt mellemliggende konsonanter behandles i én sammenhæng
		 */
		sa = p.split(sb.toString());
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(finalizeProcessing(element) + " ");
			// System.out.println(sb.toString() + "\tfinalizeProcessing 1");
		}

		/**
		 * Dubletbogstaver fjernes
		 */
		out = sb.toString().replaceAll("(.)\\1", "$1");
		// System.out.println(out + "\teliminateDoubleCharacters");

		/**
		 *
		 * Den foreløbige kode undersøges for umulige konsonantgrupper
		 */
		sa = p.split(out);
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(eliminateImpossibleConsonantCombinations(element) + " ");
			// System.out.println(sb.toString() +
			// "\teliminateImpossibleConsonantCombinations");
		}

		/**
		 * De to vokalgrupper samt mellemliggende konsonanter behandles i én sammenhæng
		 * igen
		 */
		sa = p.split(sb.toString());
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(finalizeProcessing(element) + " ");
		}
		out = sb.toString();
		// System.out.println(out + "\tfinalizeProcessing 2");

		/**
		 * Dubletkoder fjernes
		 */
		p = Pattern.compile("(\\w+)\\s+\\1");
		m = p.matcher(out);
		while (m.find()) {
			// Fejl nedenfor?
			out = out.replaceAll("(\\w+\\s+)\\1", "$1");
			// System.out.println(out + "\teliminateDoubleOutput");
		}

		return out.trim();
	}

	/**
	 * Is any of two characters a vowel?
	 *
	 * @param a
	 * @param b
	 * @return boolean
	 */
	private boolean isVowel(char a, char b) {
		final Pattern p = Pattern.compile("[aeiouyæøå]");
		final Matcher m = p.matcher(String.valueOf(a));
		if (m.matches()) {
			return true;
		}
		m.reset(String.valueOf(b));
		return m.matches();
	}

	/**
	 * Visse gamle eller fremmede staveformer narmaliseres
	 *
	 * @param input
	 * @return
	 */
	private String normaliseForeignSpellings(String input) {
		String out = input;

		// MER Tilføjelser
		out = out.replaceAll("(aa|å)", "o");
		out = out.replace("qu", "kv");
		out = out.replaceAll("(ph|pf)", "f");
		out = out.replaceAll("[üÿ]", "y");
		out = out.replaceAll("[äéëæ]", "e");
		out = out.replaceAll("[çz]", "s");
		out = out.replace('q', 'k');
		out = out.replace('w', 'v');
		out = out.replace('ö', 'ø');
		return out.replace("ž", "sj");
	}

	/**
	 * Indledningsvis omsættes visse tegn, således at en første standardisering af
	 * navnet opnås
	 *
	 * @param nextToken
	 * @return
	 */
	private String performInitialStandardization(String nextToken) {
		String out = nextToken;
		StringBuilder sb = new StringBuilder();
		final String[] patterns = { "vo", "vø", "val", "ver", "vie", "vet", "vah", "vri" };

		// MER Af, de, du, la og von fjernes
		Pattern p = Pattern.compile("(af|de|du|la|von)");
		Matcher m = p.matcher(out.trim());
		if (m.matches()) {
			return "";
		}

		// "vo", "vø", "val", "ver", "vie", "vet", "vah" og "vri"; dubleres v og
		// f
		for (final String pattern : patterns) {
			p = Pattern.compile(pattern);
			m = p.matcher(out);

			if (m.lookingAt() && m.start() == 0) {
				sb.append("v" + out.substring(1) + " f" + out.substring(1));
				out = sb.toString();
				break;
			}
		}

		/*
		 * Visse gamle eller fremmede staveformer normaliseres
		 */
		p = Pattern.compile(" ");
		String[] sa = p.split(out);
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(normaliseForeignSpellings(element) + " ");
		}

		/**
		 * x, h og fordoblinger
		 */
		sa = p.split(sb.toString());
		sb = new StringBuilder();
		for (final String element : sa) {
			sb.append(processHXAndDoubles(element) + " ");
		}
		out = sb.toString();

		/**
		 * Patronymika
		 */
		if (out.trim().length() > 3) {
			out = out.replaceAll("(sen|son|sdtr|sdater|sdoter)(\\s|\\b)", "s ");
		}

		return out;
	}

	/**
	 * Efter behandlingen af første vokalgruppe behandles evt. foranstillede
	 * konsonanter sammen med første vokal
	 *
	 * @param input
	 * @return String
	 */
	private String processFirstConsonantGroup(String input) {
		String out = input;

		// Find første konsonantgruppe
		// Afslut, hvis den ikke er foranstillet
		final Pattern p = Pattern.compile("\\b[^aeiouyæøå]+");
		final Matcher m = p.matcher(input);
		if (!m.lookingAt()) {
			return input;
		}

		// Sidste konsonant i gruppen
		// Blødt c som sidste konsonant -> s
		out = out.replaceFirst("\\b([^aceiouyæøå]*)c([eiy])", "$1s$2");

		// Hårdt c som sidste konsonant -> k
		out = out.replaceFirst("\\b([^aceiouyæøå]*)c([aouøæå])", "$1k$2");

		// Næstsidste eller før
		// Hårdt d -> t
		out = out.replaceFirst("\\b([^adeiouyæøå]*)d([^aeiouyæøå]+)", "$1t$2");

		// Hårdt g -> k
		out = out.replaceFirst("\\b([^aegiouyæøå]*)g([^aeiouyæøå]+)", "$1k$2");

		// ts -> s
		out = out.replaceFirst("\\b([^aeiotuyæøå]*)ts([^aeiouyæøå]+)", "$1s$2");

		// kj -> k
		out = out.replaceFirst("\\b([^aeikouyæøå]*)kj", "$1k");

		// Næstsidste
		// sco/u -> sko/u
		out = out.replaceFirst("\\b([^aeiosuyæøå]*)sc([ou])", "$1sk$2");

		// sc(^o/u) -> s(^o/u)
		out = out.replaceFirst("\\b([^aeiouyæøå]*)sc([aeiyøæå])", "$1s$2");

		// cs/j -> ss/j
		out = out.replaceFirst("\\b([^aceiouyæøå]*)c([sj])", "$1s$2");

		// cho -> ko
		out = out.replaceFirst("\\b([^aceiouyæøå]*)cho", "$1ko");

		// ch(V^o) -> sj(V^o)
		out = out.replaceFirst("([^aceiouyæøå]*)ch([aeiuyæøå])", "$1sj$2");

		// cV -> kV
		out = out.replaceFirst("([^aceiouyæøå]*)c([aeiouyøæå])", "$1k$2");

		// Før næstsidste
		// schov -> skov
		out = out.replaceFirst("\\b([^aeiosuyæøå]*s)ch(ov)", "$1k$2");

		// schV -> sjV
		out = out.replaceFirst("\\b([^aeiosuyæøå]*)sch([aeiouyæøå]+)", "$1sj$2");

		// MER: schr -> skr
		out = out.replaceFirst("\\b([^aeiosuyæøå]*)schr", "$1skr");

		// schC -> sC
		out = out.replaceFirst("\\b([^aeiosuyæøå]*)sch([^aeiouyæøå]+)", "$1s$2");

		// sc -> s
		out = out.replaceFirst("\\b([^aeiosuyæøå]*)sc", "$1s");

		// Bsd/t -> Bs
		out = out.replaceFirst("\\b([^aeiouyæøå]+)s[dt]", "$1s");

		// _chr/l -> _kr/l
		out = out.replaceFirst("\\b([^aeiouyæøå]*)ch([lr])", "$1k$2");

		// _chC -> _C
		out = out.replaceFirst("\\b([^aeiouyæøå]*)ch([^aeiouyæøå]+)", "$1$2");

		// ch -> k
		out = out.replaceFirst("\\b([^aceiouyæøå]*)ch", "$1k");

		// cs/j -> ss/j
		out = out.replaceFirst("\\b([^aceiouyæøå]*)c([js])", "$1s$2");

		return out.replaceFirst("\\b([^aceiouyæøå]*)c", "$1k");
	}

	/**
	 * Første vokalgruppe identificeres og omdannes efter behov. Hvis flere end to
	 * vokaler indgår i en vokalgruppe, behandles vokalerne parvis med start forfra
	 * i gruppen
	 *
	 * @param input
	 * @return String
	 */
	private String processFirstVowelGroup(String input) {
		String out = input;
		Pattern p;
		Matcher m;

		// Er der overhovedet vokaler?
		p = Pattern.compile("[aeiouyæøå]+");
		m = p.matcher(input);
		if (!m.find()) {
			return input;
		}

		// Kun én vokal i gruppen?
		if (m.group().length() < 2) {

			// y fordobles til y og ø
			final int st = m.start();
			if (input.charAt(st) == 'y') {
				return input + " " + input.substring(0, st) + 'ø' + input.substring(st + 1);
			}
			// Kun en vokal i gruppen, men ikke y
			return input;
		}

		// Der er mindst to vokaler i gruppen
		// Det undersøges, om nogen af følgende regler skal bruges til
		// omsætning. e som anden vokal er et specialtilfælde

		// [Klusil eller labiodental frikativ]ie -> []je
		out = out.replaceAll("([bdfgkptv])ie", "$1je");

		// [Klusil eller labiodental frikativ]Ve -> []e
		out = out.replaceAll("([bdfgkptv][aeiouyæøå])e", "$1");

		// eu er et specialtilfælde, som fordobles til ev og øj
		p = Pattern.compile("eu");
		m = p.matcher(out);
		if (m.matches()) {
			return out.replaceFirst("eu", "ev") + " " + out.replaceFirst("eu", "øj");
		}

		// Øvrig håndtering af vokalpar
		final String org = out;
		out = out.replaceFirst("([aeiouyæøå])i", "$1j");
		out = out.replaceFirst("[iy]([aou])", "j$1");
		out = out.replaceFirst("iø", "jø");
		out = out.replaceFirst("(u)[ao]", "v$1");
		out = out.replaceFirst("([ao])u", "$1v");
		out = out.replaceFirst("([aeoø])y", "$1j");
		if (org.equalsIgnoreCase(out)) {
			return out.replaceFirst("([aeiouyæøå])[aeiouyæøå]", "$1");
		}
		return out;
	}

	/**
	 * x, h og fordoblinger
	 *
	 * @param nextToken
	 * @return
	 */
	private String processHXAndDoubles(String input) {
		String out = input;
		Pattern p;
		Matcher m;

		// x -> s eller ks
		p = Pattern.compile("x");
		m = p.matcher(out);
		if (m.find()) {
			if (m.start() == 0) {
				out = m.replaceFirst("s");
			} else {
				out = out.replace("x", "ks");
			}
		}

		// Aspirerert eller stumt h
		out = out.replaceFirst("\\b(h[aeiouyæøå])", "$1");
		out = out.replaceAll("([^c])h", "$1");
		// MER tilføjet
		out = out.replaceFirst("\\bhv", "v");

		// gg -> k
		out = out.replace("gg", "k");

		// BB -> B
		out = out.replaceAll("(.)\\1", "$1");

		return out.replace("ck", "k");
	}

	/**
	 * Navnedelen efter første vokalgruppe behandles
	 *
	 * @param input
	 * @return
	 * @throws Exception
	 */
	private String processSecondConsonantGroup(String input) throws Exception {
		String out = input;
		StringBuffer sb;

		// Find første konsonantgruppe efter første vokalgruppe
		Pattern p = Pattern.compile("[aeiouyæøå]+[^aeiouyæøå]+");
		Matcher m = p.matcher(input);

		if (!m.find()) {
			return out;
		}

		// oj -> øj
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]*)oj", "$1øj");

		// aj -> ej
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]*)aj", "$1ej");

		// Bn/ld -> Bn/l
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*[ln])d", "$1");

		// Vd_ -> V
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+)d\\b", "$1");

		// Vdø/e -> Vø/e
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+)d([eø])", "$1$2");

		// VdC -> VC
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+)d([^aeiouyæøå])", "$1$2");

		// MER Tilføjet
		// Bd_ -> B_
		p = Pattern.compile("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*)d\\b");
		m = p.matcher(out);
		if (m.find()) {
			sb = new StringBuffer();
			m.appendReplacement(sb, "$1");
			out = sb.toString();
		} else {
			// Bd -> Bt
			out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*)d", "$1t");
		}

		// MER fjernet
		// Bm -> Bn

		// Bb -> Bp
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*)b", "$1p");

		// eg n/l ø/_/C/e -> ej n/l ø/_/C/e
		out = out.replaceFirst("([^aeiouyæøå]*[aiouyæøå]+e)g([ln])([^aiouyæå]|\\b)", "$1j$2$3");

		// Vgø/_/C -> Vø/_/C
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+)g([^aeiouyæå]|\\b)", "$1$2");

		// Vge -> V
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+)ge", "$1");

		// o/a vg ø/_/C -> o/a v ø/_/C
		out = out.replaceFirst("([^aeiouyæøå]*[eiuyæøå]*[ao]v)g([^aeiouyæå]|\\b)", "$1$2");

		// o/a vg -> o/a v
		out = out.replaceFirst("([^aeiouyæøå]*[eiuyæøå]*[ao]v)g", "$1");

		// B r/l g ø/e/_ -> B r/l ø/e/_
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*[lr])g([^aiouyæå]|\\b)", "$1$2");

		// arC -> aC, ar -> a
		out = out.replaceFirst("([^aeiouyæøå]*[eiouyæøå]*a)r([^aeiouyæøå]|\\b)", "$1$2");

		// MER
		// Bg -> Bk
		p = Pattern.compile("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*)g\\b");
		m = p.matcher(out);
		if (m.find()) {
			sb = new StringBuffer();
			m.appendReplacement(sb, "$1");
			out = sb.toString();
		} else {
			// Bg -> Bk
			out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*)g", "$1k");
		}

		// Bn g/k C/_ -> Bn C/_
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*n)[gk]([^aeiouyæøå]|\\b)", "$1$2");

		// Bs d/t -> Bs
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+[^aeiouyæøå]*s)[dt]", "$1");

		// ic_ -> is_
		out = out.replaceFirst("([^aeiouyæøå]*[aeouyæøå]*)ic\\b", "$1is");

		// av -> ov
		out = out.replaceFirst("([^aeiouyæøå]*[eiouyæøå]*)av", "$1ov");

		// schov -> skov
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+)schov", "$1skov");

		// sch V/_ -> sj V/_
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+s)ch([aeiouyæøå]|\\b)", "$1j$2");

		// schC -> sC
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+s)ch([^aeiouyæøå])", "$1$2");

		// sc o/u -> sk o/u
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+s)c([ou])", "$1k$2");

		// sc, ts -> s
		out = out.replaceFirst("([^aeiouyæøå]*[aeiouyæøå]+)(sc|ts)", "$1s");

		// c e/i/y/j/s -> s e/i/y/j/s
		out = out.replaceFirst("c([eijsy])", "s$1");

		// ch -> k
		out = out.replaceFirst("ch", "k");

		// c -> k
		out = out.replaceFirst("c", "k");

		return out.replaceFirst("kje", "ke");
	}

	/**
	 * Anden vokalgruppe behandles ligesom den første
	 *
	 * @param input
	 * @return String
	 * @throws Exception
	 */
	private String processSecondVowelGroup(String input) throws Exception {
		String out = input;
		StringBuffer sb = new StringBuffer(input);

		if (out.length() < 2) {
			return input;
		}

		// Find første vokalgruppe
		Pattern p = Pattern.compile("[aeiouyæøå]+");
		Matcher m = p.matcher(input);

		// Find en evt. 2. vokalgruppe
		if (!m.find()) {
			return input;
		}

		final int vgStart = m.start();
		final int vgSlut = m.end();

		// Kun én vokal i gruppen?
		if (vgStart == vgSlut - 1) {

			// y fordobles til y og ø
			if (input.charAt(vgStart) == 'y') {
				return input + " " + input.substring(0, vgStart) + 'ø' + input.substring(vgStart + 1);
			}
			// Kun en vokal i gruppen, men ikke y
			return input;
		}

		// Der er mindst to vokaler i gruppen
		// Det undersøges, om nogen af følgende regler skal bruges til
		// omsætning. e som anden vokal er et specialtilfælde

		// [Klusil eller labiodental frikativ]ie -> []je
		out = out.replaceAll("([bdfgkptv])ie", "$1je");

		// [Klusil eller labiodental frikativ]Ve -> []e
		p = Pattern.compile("([bdfgkptv][aeiouyæøå])e");
		m = p.matcher(out);
		if (m.find()) {
			sb = new StringBuffer();
			m.appendReplacement(sb, "$1");
			m.appendTail(sb);
			out = sb.toString();
		}

		// eu er et specialtilfælde, som fordobles til ev og øj
		p = Pattern.compile("eu");
		m = p.matcher(out);
		if (m.matches()) {
			return out.replaceFirst("eu", "ev") + " " + out.replaceFirst("eu", "øj");
		}

		// Øvrig håndtering af vokalpar
		final String org = out;
		out = out.replaceFirst("([aeiouyæøå])i", "$1j");
		out = out.replaceFirst("[iy]([aou])", "j$1");
		out = out.replaceFirst("iø", "jø");
		out = out.replaceFirst("(u)[ao]", "v$1");
		out = out.replaceFirst("([ao])u", "$1v");
		out = out.replaceFirst("([aeoø])y", "$1j");
		if (org.equalsIgnoreCase(out)) {
			return out.replaceFirst("([aeiouyæøå])[aeiouyæøå]", "$1");
		}
		return out;
	}

}
