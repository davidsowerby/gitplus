package uk.q3c.build.gitplus.creator.gradle.element

import spock.lang.Specification
import uk.q3c.KotlinObjectFactory
import uk.q3c.build.gitplus.creator.gradle.buffer.FileBuffer

/**
 * Created by David Sowerby on 07 Oct 2016
 */
class BasicScriptElementTest extends Specification {

    BasicScriptElement basicScriptElement
    FileBuffer fileBuffer


    def "equals and hashCode"() {
        given:
        BasicScriptElement a = new BasicScriptElement('a')
        BasicScriptElement b = new BasicScriptElement('b')
        BasicScriptElement a1 = new BasicScriptElement('a')

        expect:
        a.equals(a)
        a.equals(a1)
        !a.equals(b)
        a.hashCode() == a1.hashCode()
        a.hashCode() != b.hashCode()

        !a.equals(null)
        !a.equals("x")
    }

    def "to String and formatContent"() {
        given:
        BasicScriptElement a = new BasicScriptElement('a')

        expect:
        a.toString() == 'a'
        a.formatContent() == 'a'
    }

    def "write() processes content with formatContent() and stores in formattedContent"() {
        given:
        BasicScriptElement a = new BasicScriptElement('a')
        fileBuffer = KotlinObjectFactory.fileBuffer()

        when:
        a.write()

        then:
        a.formattedContent == a.content
        fileBuffer.output().contains("a\n")

    }
}
