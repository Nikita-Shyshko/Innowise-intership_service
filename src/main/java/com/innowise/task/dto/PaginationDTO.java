package com.innowise.task.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PaginationDTO
{
    private Integer pageNumber;
    private Integer pageSize;
    private String sortedBy;

    public PaginationDTO(Integer pageNumber, Integer pageSize, String sortedBy)
    {
        this.pageNumber = pageNumber != null ? pageNumber : 0;
        this.pageSize = pageSize != null ? pageSize : 30;
        this.sortedBy = sortedBy != null ? sortedBy : "id";
    }
}
