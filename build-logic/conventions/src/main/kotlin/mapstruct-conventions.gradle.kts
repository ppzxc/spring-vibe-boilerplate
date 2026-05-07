// 골격: 현재 사용 모듈 0개. MapStruct를 사용하는 어댑터 모듈이 생기면 적용.
plugins {
  id("java-conventions")
}

dependencies {
  implementation(libs.org.mapstruct)
  annotationProcessor(libs.org.projectlombok.mapstruct.binding)
  annotationProcessor(libs.org.mapstruct.processor)
}
