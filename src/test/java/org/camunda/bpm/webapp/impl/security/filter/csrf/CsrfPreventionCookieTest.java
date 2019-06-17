/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.webapp.impl.security.filter.csrf;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.webapp.impl.util.HeaderRule;
import org.junit.Rule;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class CsrfPreventionCookieTest {

  @Rule
  public HeaderRule headerRule = new HeaderRule();

  @Test
  public void shouldConfigureDefault() {
    // given
    headerRule.startServer("web.xml", "csrf");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getCookieHeader())
      .doesNotContain(";HttpOnly")
      .contains(";SameSite=Strict")
      .doesNotContain(";Secure");
  }

  @Test
  public void shouldConfigureSecureEnabled() {
    // given
    headerRule.startServer("secure_enabled_web.xml", "csrf");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getCookieHeader()).contains(";Secure");
  }

  @Test
  public void shouldConfigureSameSiteDisabled() {
    // given
    headerRule.startServer("same_site_disabled_web.xml", "csrf");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getCookieHeader()).doesNotContain(";SameSite");
  }

  @Test
  public void shouldConfigureSameSiteOptionStrict() {
    // given
    headerRule.startServer("same_site_option_strict_web.xml", "csrf");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getCookieHeader()).contains(";SameSite=Strict");
  }

  @Test
  public void shouldConfigureSameSiteOptionLax() {
    // given
    headerRule.startServer("same_site_option_lax_web.xml", "csrf");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getCookieHeader()).contains(";SameSite=Lax");
  }

  @Test
  public void shouldConfigureSameSiteCustomValue() {
    // given
    headerRule.startServer("same_site_custom_value_web.xml", "csrf");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getCookieHeader()).contains(";SameSite=aCustomValue");
  }

  @Test
  public void shouldThrowExceptionWhenConfiguringBothSameSiteOptionAndValue() {
    // given
    headerRule.startServer("same_site_option_value_web.xml", "csrf");

    // when
    headerRule.performRequest();

    Throwable expectedException = headerRule.getException();

    // then
    assertThat(expectedException)
      .isInstanceOf(ProcessEngineException.class)
      .hasMessage("Please either configure sameSiteCookieOption or sameSiteCookieValue.");
  }

  @Test
  public void shouldThrowExceptionWhenConfiguringUnknownSameSiteOption() {
    // given
    headerRule.startServer("same_site_option_unknown_web.xml", "csrf");

    // when
    headerRule.performRequest();

    Throwable expectedException = headerRule.getException();

    // then
    assertThat(expectedException)
      .isInstanceOf(ProcessEngineException.class)
      .hasMessage("For sameSiteCookieOption param, please configure one of the following options: [LAX, STRICT]");
  }

  @Test
  public void shouldIgnoreCaseOfParamValues() {
    // given
    headerRule.startServer("ignore_case_web.xml", "csrf");

    // when
    headerRule.performRequest();

    // then
    assertThat(headerRule.getCookieHeader())
      .contains(";SameSite=Lax")
      .contains(";Secure");
  }

  @Test
  public void shouldConfigureWhenCookieIsSent() {
    // given
    headerRule.startServer("web.xml", "csrf");

    // when
    headerRule.performRequestWithHeader("Cookie", "XSRF-TOKEN=aToken");

    // then
    assertThat(headerRule.getCookieHeader())
      .contains(";SameSite=Strict")
      .doesNotContain(";Secure");
  }

}
