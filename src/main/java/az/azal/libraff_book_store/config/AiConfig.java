package az.azal.libraff_book_store.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class AiConfig {

	@Bean
	ChatClient chatClient(ChatClient.Builder builder) {
		return builder
				.defaultSystem("""
						Sən Libraff Book Store kitab mağazalar şəbəkəsinin köməkçi assistentisən.
						Yalnız verilən kontekstə əsasən cavab ver.
						Kontekstdə cavab yoxdursa, "Bu barədə məlumatım yoxdur" de.
						Cavabları qısa və dəqiq ver. Azərbaycan dilində cavab ver.
						""")
				.build();
	}

	@Bean
	VectorStore vectorStore(EmbeddingModel embeddingModel) {
		SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

		File dataDir = new File("ai-data");
		if (!dataDir.exists()) {
			dataDir.mkdirs();
		}

		File vectorFile = new File(dataDir, "vector-store.json");
		if (vectorFile.exists() && vectorFile.length() > 0) {
			try {
				store.load(vectorFile);
				System.out.println("Vektor bazası fayldan yükləndi: " + vectorFile.getAbsolutePath());
			} catch (Exception e) {
				System.err.println(
						"Vektor faylı oxuna bilmədi (ola bilsin zədələnib və ya yarımçıq qalıb): " + e.getMessage());
				System.err.println("Korlanmış fayl silinir, yeni baza yaradılacaq...");
				vectorFile.delete();
			}
		} else {
			System.out.println("Vektor bazası boşdur (və ya yoxdur). /api/ai/documents ilə sənəd əlavə edin.");
		}

		return store;
	}
}
