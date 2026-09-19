package az.azal.libraff_book_store.service;

import az.azal.libraff_book_store.config.ToolsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {

	private final ChatClient chatClient;
	@Autowired
	private VectorStore vectorStore;
	private final ToolsConfig toolsConfig;
	private final EmbeddingModel embeddingModel;

	public void addDocuments(List<String> texts) {
		File vectorFile = new File("ai-data", "vector-store.json");
		vectorStore = SimpleVectorStore.builder(embeddingModel).build();

		if (vectorFile.exists()) {
			vectorFile.delete();
			System.out.println("Köhnə vektor faylı silindi.");
		}

		int CHUNK_SIZE = 800;    
		int OVERLAP = 200;      

		List<Document> documents = texts.stream()
				.map(text -> splitTextIntoChunks(text, CHUNK_SIZE, OVERLAP))
				.flatMap(List::stream)
				.map(Document::new)      
				.toList();

		vectorStore.add(documents);
		saveVectorStore();
	}

	public Map<String, String> askWithRag(String question) {

		if (vectorStore == null) {
			return Map.of("answer", "Əvvəlcə sənəd əlavə edin.", "context", "");
		}

		List<Document> relevantDocs = vectorStore.similaritySearch(
				SearchRequest.builder()
						.topK(3)                   
						.similarityThreshold(0.3)  
						.build());

		String context = relevantDocs.stream()
				.map(Document::getText)
				.collect(Collectors.joining("\n---\n"));

		if (context.isBlank()) {
			return Map.of(
					"answer", "Bilgi bazasında bu suala aid məlumat tapılmadı.",
					"context", "");
		}

		String answer = chatClient.prompt()
				.user("Kontekst:\n" + context + "\n\nSual: " + question)
				.call()
				.content();

		return Map.of(
				"answer", answer,
				"context", context);
	}

	public String askSimple(String question) {
		return chatClient.prompt()
				.user(question)
				.call()
				.content();
	}

	public String askWithTools(String question) {
		return chatClient.prompt()
				.user(question)
				.tools(toolsConfig)
				.call()
				.content();
	}

	private void saveVectorStore() {
		if (vectorStore instanceof SimpleVectorStore simpleStore) {
			File vectorFile = new File("ai-data", "vector-store.json");
			simpleStore.save(vectorFile);
			System.out.println("Vektor bazası faylda saxlandı: " + vectorFile.getAbsolutePath());
		}
	}

	private List<String> splitTextIntoChunks(String text, int chunkSize, int overlapSize) {
		List<String> chunks = new ArrayList<>();
		if (text == null || text.isBlank())
			return chunks;
		if (text.length() <= chunkSize) {
			chunks.add(text);
			return chunks;
		}

		int startIndex = 0;
		while (startIndex < text.length()) {
			int endIndex = Math.min(startIndex + chunkSize, text.length());
			chunks.add(text.substring(startIndex, endIndex));
			if (endIndex == text.length()) {
				break;
			}
			startIndex = endIndex - overlapSize;
		}
		return chunks;
	}
}
