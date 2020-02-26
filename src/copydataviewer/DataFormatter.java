package copydataviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import copydataviewer.util.SystemUtil;
import lombok.AllArgsConstructor;
import lombok.Data;

public class DataFormatter {
	
	private static final String LINE_SEP = "\r";
	private static final Pattern COPY_DECLARATION = Pattern
			.compile("[0-9]+\\s+(.+)\\s+PIC\\s+.+\\(([0-9]+)\\)\\s*\\.");
	private static final Pattern LINE_SEP_PATTERN = Pattern.compile("[\\r\\n]+");
	private String csvSep;

	@Data
	@AllArgsConstructor
	private static class CopyField {
		String name;
		int length;
	}

	public DataFormatter(String csvSep) {
		this.csvSep = csvSep;
		
	}
	//	private static String listToString(List<String> list, String sep) {
	//	return streamToString(list.stream(), sep);
	//}

	private static String streamToString(Stream<String> stream, String sep) {
		return stream.collect(Collectors.joining(sep));
	} 

	public String format(String copyContent, String data) {
		return this.getDatasWithSepAndTitle(data, getFieldsLength(copyContent), this.csvSep);
	}

	
	/**
	 * Transformation de chaque ligne des données d'entrée (data) en ligne de champs
	 * séparés par le séparateur spécifié (sep) grâce au format de la copy
	 * (copyFields)
	 */
	private String getDatasWithSepAndTitle(String data, List<CopyField> copyFields, String sep) {
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
	private List<CopyField> getFieldsLength(String copyContent) {
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
