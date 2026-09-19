package az.azal.libraff_book_store.controller;

import az.azal.libraff_book_store.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(originPatterns = "*")
public class RagController {

	private final RagService ragService;

	public RagController(RagService ragService) {
		this.ragService = ragService;
	}

	@PreAuthorize("hasAuthority('ROLE_ADD_DOCUMENT')")
	@PostMapping("/documents")
	public ResponseEntity<Map<String, String>> addDocuments(@RequestBody Map<String, List<String>> request) {
		List<String> texts = request.get("texts");
		ragService.addDocuments(texts);
		return ResponseEntity.ok(Map.of(
				"status", "OK",
				"message", texts.size() + " sənəd bilgi bazasına əlavə edildi"));
	}

	@PreAuthorize("hasAuthority('ROLE_USE_AI')")
	@PostMapping("/ask")
	public ResponseEntity<Map<String, String>> askWithRag(@RequestBody Map<String, String> request) {
		String question = request.get("question");
		Map<String, String> result = ragService.askWithRag(question);

		return ResponseEntity.ok(Map.of(
				"question", question,
				"answer", result.get("answer"),
				"context", result.get("context"),
				"type", "RAG"));
	}

	@PreAuthorize("hasAuthority('ROLE_USE_AI')")
	@GetMapping("/ask/simple")
	public ResponseEntity<Map<String, String>> askSimple(@RequestParam String q) {
		String answer = ragService.askSimple(q);
		return ResponseEntity.ok(Map.of(
				"question", q,
				"answer", answer,
				"type", "SIMPLE (no RAG)"));
	}

	@PreAuthorize("hasAuthority('ROLE_USE_AI')")
	@PostMapping("/ask/with-tools")
	public ResponseEntity<Map<String, String>> askWithTools(@RequestBody Map<String, String> request) {
		String question = request.get("question");
		String answer = ragService.askWithTools(question);
		return ResponseEntity.ok(Map.of(
				"question", question,
				"answer", answer,
				"type", "FUNCTION_CALLING"));
	}
}
