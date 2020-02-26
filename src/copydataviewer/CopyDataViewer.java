package copydataviewer;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Data;
import util.copyDataViewer.SystemUtil;

/** 
 * Visualiseur de données au format fixe (Cobol)
 * Chaque ligne est composée de champs de longueur déterminée
 * La structure de chaque champs est définie par une copy Cobol.
 * Attention: la copy Cobol doit être simple (non prise en compte de V9(9)S0(2) par exemple) => cf. pattern "COPY_DECLARATION" dans le code
 * 	10 XXXX PIC X (9).
 *  10 XXXX PIC S9 (9).
 *  
 *  Entrée: un fichier copy et un fichier de données
 *  Sortie: un fichier CSV avec le séparateur spécifié par "CSV_SEP" (cf. code ci-dessous)
 * 
 * */
public class CopyDataViewer {

	private static final String CSV_SEP = ";";
	private static final String LINE_SEP = "\r";
	private static final Pattern COPY_DECLARATION = Pattern
			.compile("[0-9]+\\s+(.+)\\s+PIC\\s+.+\\(([0-9]+)\\)\\s*\\.");
	private static final Pattern LINE_SEP_PATTERN = Pattern.compile("[\\r\\n]+");

	public static void main(String[] args) throws IOException {

		String copyContent = SystemUtil.getFileContent(Paths.get(args[0]));
		String dataContent = SystemUtil.getFileContent(Paths.get(args[1]));

		List<CopyField> copyFields = getFieldsLength(copyContent);
		String newDataContent = getDatasWithSepAndTitle(dataContent, copyFields, CSV_SEP);

		SystemUtil.writeFileWithContent(args[1] + ".csv", newDataContent);
	}

//	private static String listToString(List<String> list, String sep) {
//		return streamToString(list.stream(), sep);
//	}

	private static String streamToString(Stream<String> stream, String sep) {
		return stream.collect(Collectors.joining(sep));
	}

	@Data
	@AllArgsConstructor
	private static class CopyField {
		String name;
		int length;
	}

	/**
	 * Transformation de chaque ligne des données d'entrée (data) en ligne de champs
	 * séparés par le séparateur spécifié (sep) grâce au format de la copy
	 * (copyFields)
	 */
	private static String getDatasWithSepAndTitle(String data, List<CopyField> copyFields, String sep) {
		StringBuilder dataWithSepAndTitle = new StringBuilder();
		dataWithSepAndTitle
				.append(streamToString(copyFields.stream().map(c -> c.getName() + " (" + c.getLength() + ")"), sep));
		dataWithSepAndTitle.append(LINE_SEP);
		List<String> dataLines = SystemUtil.splitContent(data, LINE_SEP_PATTERN);

		// Parcours des lignes de données
		for (String line : dataLines) {
			StringBuilder sb = new StringBuilder();
			int startIdx = 0;
			int endIdx = 0;
			// Découpage de chaque ligne suivant les champs de la copy
			for (CopyField copyField : copyFields) {
				// endIdx prend la valeur de la longueur du champ de la copie
				endIdx = startIdx + copyField.getLength();
				// Si enddIdx dépasse la longueur il est ramené à la longueur de la ligne
				if (line.length() < startIdx + copyField.getLength()) {
					endIdx = line.length();
				}
				// Si startIdx n'est pas supérieur à la longueur de la ligne alors ajout du
				// champ (de startIdx à endIdx) suivi d'un séparateur
				if (startIdx < line.length()) {
					sb.append(line.substring(startIdx, endIdx));
					sb.append(sep);
				}
				// startIdx prend la valeur de la longueur du champ qui vient d'être ajouté pour
				// repartir au champ suivant
				startIdx += copyField.getLength();
			}

			// Si tous les champs de la copy ont été ajoutés et qu'il reste des données
			// alors on les ajoutes à la fin
			if (endIdx < line.length()) {
				sb.append(line.substring(endIdx));
			}

			// Ajout de la ligne formatée en CSV
			dataWithSepAndTitle.append(sb).append(LINE_SEP);
		}

		return dataWithSepAndTitle.toString();
	}

	/**
	 * Construction de la liste des champs avec leur longueur à partir du contenu
	 * d'un fichier copy
	 */
	private static List<CopyField> getFieldsLength(String copyContent) {
		List<CopyField> copyFields = new ArrayList<>();
		Matcher matcher = COPY_DECLARATION.matcher(copyContent);
		// Parcours des déclarations des champs de la copy
		while (matcher.find()) {
			var fieldName = matcher.group(1);
			var fieldLength = matcher.group(2);
			copyFields.add(new CopyField(fieldName, Integer.parseInt(fieldLength)));
		}
		return copyFields;
	}

}
