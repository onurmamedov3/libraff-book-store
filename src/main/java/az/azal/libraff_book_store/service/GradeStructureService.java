package az.azal.libraff_book_store.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import az.azal.libraff_book_store.entity.GradePositionEntity;
import az.azal.libraff_book_store.entity.GradeStoreEntity;
import az.azal.libraff_book_store.entity.GradeStructureEntity;
import az.azal.libraff_book_store.entity.PositionEntity;
import az.azal.libraff_book_store.entity.StoreEntity;
import az.azal.libraff_book_store.enums.ErrorStatus;
import az.azal.libraff_book_store.enums.GradeTarget;
import az.azal.libraff_book_store.exception.MyException;
import az.azal.libraff_book_store.repository.GradePositionRepository;
import az.azal.libraff_book_store.repository.GradeStoreRepository;
import az.azal.libraff_book_store.repository.GradeStructureRepository;
import az.azal.libraff_book_store.repository.PositionRepository;
import az.azal.libraff_book_store.repository.StoreRepository;
import az.azal.libraff_book_store.request.GradeStructureAddRequest;
import az.azal.libraff_book_store.response.GradeListResponse;
import az.azal.libraff_book_store.response.GradeSingleResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GradeStructureService {

	private final GradeStructureRepository repository;

	private final GradeStoreRepository gradeStoreRepository;

	private final GradePositionRepository gradePositionRepository;

	private final StoreRepository storeRepository;

	private final PositionRepository positionRepository;

	private final ModelMapper mapper;

	@Transactional
	public void addGrade(GradeStructureAddRequest request) {
		GradeStructureEntity grade = new GradeStructureEntity();

		mapper.map(request, grade);

		repository.save(grade);

		if (grade.getTargetType() == GradeTarget.STORE && request.getAssignedStoreIds() != null) {
			for (Integer storeId : request.getAssignedStoreIds()) {
				StoreEntity store = storeRepository.findById(storeId).orElseThrow(
						() -> new MyException("Store ID " + storeId + " not found", ErrorStatus.STORE_NOT_FOUND));

				GradeStoreEntity gradeStore = new GradeStoreEntity();
				gradeStore.setGradeStructure(grade);
				gradeStore.setStore(store);
				gradeStoreRepository.save(gradeStore);
			}
		}

		if (grade.getTargetType() == GradeTarget.EMPLOYEE && request.getAssignedPositionIds() != null) {
			for (Integer positionId : request.getAssignedPositionIds()) {
				PositionEntity position = positionRepository.findById(positionId)
						.orElseThrow(() -> new MyException("Position ID " + positionId + " not found",
								ErrorStatus.POSITION_NOT_FOUND));

				GradePositionEntity gradePosition = new GradePositionEntity();
				gradePosition.setGradeStructure(grade);
				gradePosition.setPosition(position);
				gradePositionRepository.save(gradePosition);
			}
		}

	}

	public GradeListResponse getAllGrades() {

		List<GradeStructureEntity> grades = repository.findAll();
		List<GradeSingleResponse> responseList = new ArrayList<GradeSingleResponse>();

		for (GradeStructureEntity grade : grades) {
			GradeSingleResponse response = new GradeSingleResponse();
			mapper.map(grade, response);
			responseList.add(response);
		}
		GradeListResponse listResponse = new GradeListResponse();
		listResponse.setGrades(responseList);

		return listResponse;
	}

}
