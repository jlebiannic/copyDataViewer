package copydataviewer;

import java.io.IOException;
import java.nio.file.Paths;

import copydataviewer.util.SystemUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

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

	public static void main(String[] args) throws IOException {

		Params params = getParams(args);

		if (params != null) {

			String copyContent = SystemUtil.getFileContent(Paths.get(params.getCopy()));
			String dataContent = SystemUtil.getFileContent(Paths.get(params.getData()));

			DataFormatter dataFormatter = new DataFormatter(params.getSep() == null ? CSV_SEP : params.getSep());
			var newDataContent = dataFormatter.format(copyContent, dataContent);
			SystemUtil.writeFileWithContent(params.getRes() == null ? params.getData() + ".csv" : params.getRes(), newDataContent);
		}
	}


	
	/**
	 * Analyse des arguments passés au programme
	 */
	public static Params getParams(String[] args) {
		Params params = null;
		ArgumentParser parser = ArgumentParsers.newFor("CopyDataViewer").build()
				.description("Formate un fichier de donnees à l'aide d'une copy Cobol");
		parser.addArgument("-copy")
				.dest("copy").metavar("fichier copy")
				.type(String.class)
				.required(true)
				.help("chemin du fichier copy");
		
		parser.addArgument("-data")
				.dest("data").metavar("fichier de donnees")
				.type(String.class)
				.required(true)
				.help("Chemin du fichier de donnees");
		
		parser.addArgument("-res")
				.dest("res").metavar("nom fichier resultat")
				.type(String.class)
				.required(false)
				.help("Chemin fichier resultat (nom du fichier de donnees suffixe par .csv par defaut)");

		parser.addArgument("-sep").dest("sep").metavar("separateur pour le fichier CSV").type(String.class)
				.required(false).help("Separateur pour le fichier CSV (';' par defaut)");
		
		try {
			Namespace res = parser.parseArgs(args);
			params = new Params(res.get("copy"), res.get("data"), res.get("res"), res.get("sep"));
		} catch (ArgumentParserException e) {
			parser.handleError(e);
		}

		return params;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Params {
		String copy;
		String data;
		String res;
		String sep;
	}

}
