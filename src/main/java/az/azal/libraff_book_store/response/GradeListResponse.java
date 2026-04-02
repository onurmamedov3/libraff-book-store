package az.azal.libraff_book_store.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GradeListResponse {

	public List<GradeSingleResponse> grades;

}
