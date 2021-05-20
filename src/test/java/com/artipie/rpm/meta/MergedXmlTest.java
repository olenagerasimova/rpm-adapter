/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 artipie.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.rpm.meta;

import com.artipie.asto.test.TestResource;
import com.artipie.rpm.Digest;
import com.artipie.rpm.TestRpm;
import com.jcabi.matchers.XhtmlMatchers;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test for {@link MergedXml}.
 * @since 1.5
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MagicNumberCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class MergedXmlTest {

    @ParameterizedTest
    @ValueSource(strings = {"other", "filelists"})
    void addsRecords(final String filename) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final TestRpm.Libdeflt libdeflt = new TestRpm.Libdeflt();
        try (InputStream input =
            new TestResource(String.format("repodata/%s.xml.example", filename)).asInputStream()) {
            final XmlPackage type = XmlPackage.valueOf(filename.toUpperCase(Locale.US));
            new MergedXml(
                input, out, type,
                new MergedPrimaryXml.Result(3L, Collections.emptyList())
            ).merge(
                new MapOf<Path, String>(
                    new MapEntry<>(libdeflt.path(), libdeflt.path().getFileName().toString())
                ),
                Digest.SHA256, this.event(type)
            );
            final String actual = out.toString(StandardCharsets.UTF_8.name());
            MatcherAssert.assertThat(
                actual,
                XhtmlMatchers.hasXPaths(
                    // @checkstyle LineLengthCheck (4 lines)
                    String.format("/*[local-name()='%s' and @packages='3']", type.tag()),
                    String.format("/*[local-name()='%s']/*[local-name()='package' and @name='aom']", type.tag()),
                    String.format("/*[local-name()='%s']/*[local-name()='package' and @name='nginx']", type.tag()),
                    String.format("/*[local-name()='%s']/*[local-name()='package' and @name='libdeflt1_0']", type.tag())
                )
            );
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"other", "filelists"})
    void replacesAndAddsRecord(final String filename) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final TestRpm libdeflt = new TestRpm.Libdeflt();
        final TestRpm time = new TestRpm.Time();
        try (InputStream input = new TestResource(
            String.format("repodata/MergedXmlTest/libdeflt-%s.xml.example", filename)
        ).asInputStream()
        ) {
            final XmlPackage type = XmlPackage.valueOf(filename.toUpperCase(Locale.US));
            new MergedXml(
                input, out, type,
                new MergedPrimaryXml.Result(2L, Collections.singleton("abc123"))
            ).merge(
                new MapOf<Path, String>(
                    new MapEntry<>(libdeflt.path(), libdeflt.path().getFileName().toString()),
                    new MapEntry<>(time.path(), time.path().getFileName().toString())
                ),
                Digest.SHA256, this.event(type)
            );
            final String actual = out.toString(StandardCharsets.UTF_8.name());
            MatcherAssert.assertThat(
                actual,
                XhtmlMatchers.hasXPaths(
                    // @checkstyle LineLengthCheck (4 lines)
                    String.format("/*[local-name()='%s' and @packages='2']", type.tag()),
                    String.format("/*[local-name()='%s']/*[local-name()='package' and @name='libdeflt1_0' and @pkgid='47bbb8b2401e8853812e6340f4197252b92463c132f64a257e18c0c8c83ae462']", type.tag()),
                    String.format("/*[local-name()='%s']/*[local-name()='package' and @name='time']", type.tag())
                )
            );
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"other", "filelists"})
    void appendsSeveralPackages(final String filename) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final TestRpm libdeflt = new TestRpm.Libdeflt();
        final TestRpm time = new TestRpm.Time();
        final TestRpm abc = new TestRpm.Abc();
        try (InputStream input = new TestResource(
            String.format("repodata/MergedXmlTest/libdeflt-nginx-%s.xml.example", filename)
        ).asInputStream()
        ) {
            final XmlPackage type = XmlPackage.valueOf(filename.toUpperCase(Locale.US));
            new MergedXml(
                input, out, type,
                new MergedPrimaryXml.Result(4L, Collections.singleton("abc123"))
            ).merge(
                new MapOf<Path, String>(
                    new MapEntry<>(libdeflt.path(), libdeflt.path().getFileName().toString()),
                    new MapEntry<>(time.path(), time.path().getFileName().toString()),
                    new MapEntry<>(abc.path(), abc.path().getFileName().toString())
                ),
                Digest.SHA256, this.event(type)
            );
            final String actual = out.toString(StandardCharsets.UTF_8.name());
            MatcherAssert.assertThat(
                actual,
                XhtmlMatchers.hasXPaths(
                    // @checkstyle LineLengthCheck (4 lines)
                    String.format("/*[local-name()='%s' and @packages='4']", type.tag()),
                    String.format("/*[local-name()='%s']/*[local-name()='package' and @name='libdeflt1_0' and @pkgid='47bbb8b2401e8853812e6340f4197252b92463c132f64a257e18c0c8c83ae462']", type.tag()),
                    String.format("/*[local-name()='%s']/*[local-name()='package' and @name='nginx']", type.tag()),
                    String.format("/*[local-name()='%s']/*[local-name()='package' and @name='abc']", type.tag())
                )
            );
        }
    }

    private XmlEvent event(final XmlPackage xml) {
        final XmlEvent res;
        if (xml == XmlPackage.OTHER) {
            res = new XmlEvent.Other();
        } else {
            res = new XmlEvent.Filelists();
        }
        return res;
    }
}