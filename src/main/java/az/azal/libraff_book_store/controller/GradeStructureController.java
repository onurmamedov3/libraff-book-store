package az.azal.libraff_book_store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import az.azal.libraff_book_store.request.GradeStructureAddRequest;
import az.azal.libraff_book_store.response.GradeListResponse;
import az.azal.libraff_book_store.service.GradeStructureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(path = "/grades")
@RequiredArgsConstructor
public class GradeStructureController {

	private final GradeStructureService service;

	@PostMapping
	public ResponseEntity<?> addGrade(@Valid @RequestBody GradeStructureAddRequest request) {

		service.addGrade(request);

		return new ResponseEntity<>("Grade successfully added!", HttpStatus.CREATED);
	}

	@GetMapping
	public GradeListResponse getAllGrades() {

		return service.getAllGrades();
	}

}
