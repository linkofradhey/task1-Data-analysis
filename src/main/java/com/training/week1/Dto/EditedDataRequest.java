package com.training.week1.Dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditedDataRequest {

	private List<String> headers;
	private List<Map<String, String>> rows;

}