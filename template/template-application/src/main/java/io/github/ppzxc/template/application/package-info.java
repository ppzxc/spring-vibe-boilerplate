/**
 * 유즈케이스 레이어. Spring 의존 금지.
 *
 * <ul>
 *   <li>{@code port.input.command.*} — Inbound Command Port (*UseCase interface)
 *   <li>{@code port.input.query.*} — Inbound Query Port (*Query interface)
 *   <li>{@code port.output.command.*} — Outbound Command Port (Save*Port interface)
 *   <li>{@code port.output.query.*} — Outbound Query Port (Find*Port interface)
 *   <li>{@code port.output.shared.*} — 공유 인프라 Port interface
 *   <li>{@code service.command.*} — Command UseCase 구현체 (*Service)
 *   <li>{@code service.query.*} — Query UseCase 구현체 (*Service)
 * </ul>
 */
package io.github.ppzxc.template.application;
