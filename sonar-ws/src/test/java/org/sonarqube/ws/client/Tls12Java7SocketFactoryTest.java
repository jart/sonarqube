/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarqube.ws.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Tls12Java7SocketFactoryTest {

  SSLSocketFactory delegate = mock(SSLSocketFactory.class);
  Tls12Java7SocketFactory underTest = new Tls12Java7SocketFactory(delegate);

  @Test
  public void createSocket_1() throws IOException {
    InetAddress address = mock(InetAddress.class);
    SSLSocket socket = mock(SSLSocket.class);
    when(delegate.createSocket(address, 80)).thenReturn(socket);
    socket = (SSLSocket) underTest.createSocket(address, 80);
    verify(socket).setEnabledProtocols(Tls12Java7SocketFactory.TLS_PROTOCOLS);
  }

  @Test
  public void createSocket_2() throws IOException {
    InetAddress address = mock(InetAddress.class);
    InetAddress address2 = mock(InetAddress.class);
    SSLSocket socket = mock(SSLSocket.class);
    when(delegate.createSocket(address, 80, address2, 443)).thenReturn(socket);
    socket = (SSLSocket) underTest.createSocket(address, 80, address2, 443);
    verify(socket).setEnabledProtocols(Tls12Java7SocketFactory.TLS_PROTOCOLS);
  }

  @Test
  public void createSocket_3() throws IOException {
    SSLSocket socket = mock(SSLSocket.class);
    when(delegate.createSocket("", 80)).thenReturn(socket);
    socket = (SSLSocket) underTest.createSocket("", 80);
    verify(socket).setEnabledProtocols(Tls12Java7SocketFactory.TLS_PROTOCOLS);
  }

  @Test
  public void support_non_ssl_sockets() throws IOException {
    Socket regularSocket = mock(Socket.class);
    when(delegate.createSocket("", 80)).thenReturn(regularSocket);
    assertThat(underTest.createSocket("", 80)).isNotInstanceOf(SSLSocket.class);
  }

  @Test
  public void delegate_getters() {
    String[] defaultCipherSuites = new String[0];
    String[] supportedCipherSuites = new String[0];
    when(delegate.getDefaultCipherSuites()).thenReturn(defaultCipherSuites);
    when(delegate.getSupportedCipherSuites()).thenReturn(supportedCipherSuites);

    assertThat(underTest.getDefaultCipherSuites()).isSameAs(defaultCipherSuites);
    assertThat(underTest.getSupportedCipherSuites()).isSameAs(supportedCipherSuites);
  }
}
