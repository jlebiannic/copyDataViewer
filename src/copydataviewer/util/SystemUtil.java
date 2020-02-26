package copydataviewer.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SystemUtil {

	private SystemUtil() {
		// empty
	}



	public static String getFileContent(Path path) throws IOException {
		return new String(Files.readAllBytes(path), Charset.forName(StandardCharsets.UTF_8.name()));
	}

	public static List<String> splitContent(String content, Pattern pattern) {
		return Arrays.asList(pattern.split(content)).stream().map(x -> x).collect(Collectors.toList());
	}

	public static void writeFileWithContent(String resultFileName, String content) throws IOException {
		Files.write(Paths.get(resultFileName), content.getBytes());
	}

	// public static String toStringJava9(InputStream inputStream) {
	// try {
	// return new String(inputStream.readAllBytes());
	// } catch (IOException e) {
	// throw new RuntimeException(e);
	// }
	// }

}

