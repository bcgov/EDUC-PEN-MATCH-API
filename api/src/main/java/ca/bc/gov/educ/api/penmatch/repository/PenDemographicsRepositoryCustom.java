package ca.bc.gov.educ.api.penmatch.repository;

import java.util.List;

import ca.bc.gov.educ.api.penmatch.model.PenDemographicsEntity;

public interface PenDemographicsRepositoryCustom {
  List<PenDemographicsEntity> searchPenDemographics(String studSurName, String studGiven, String studMiddle, String studBirth, String studSex);
}
