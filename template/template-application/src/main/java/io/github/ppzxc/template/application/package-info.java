/**
 * template-application: Inbound Port + Outbound Port + UseCase 구현체.
 *
 * <p>Spring 의존 금지. 순수 Java 인터페이스와 구현체만 허용.
 *
 * <p>패키지 구조:
 *
 * <ul>
 *   <li>{@code port.in.command.*} — Inbound Command UseCase 인터페이스
 *   <li>{@code port.in.query.*} — Inbound Query 인터페이스
 *   <li>{@code port.out.command.*} — Outbound Command Port 인터페이스 (Save*)
 *   <li>{@code port.out.query.*} — Outbound Query Port 인터페이스 (Find*)
 *   <li>{@code port.out.shared.*} — 공유 인프라 Port 인터페이스
 *   <li>{@code service.command.*} — Command UseCase 구현체
 *   <li>{@code service.query.*} — Query 구현체
 * </ul>
 */
package io.github.ppzxc.template.application;
