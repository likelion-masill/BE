package project.masil.global.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListResponse<T> {

  private int totalCount;

  private List<T> items;

}