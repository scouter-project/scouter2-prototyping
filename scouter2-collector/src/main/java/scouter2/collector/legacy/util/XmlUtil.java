/*
 * Copyright 2019. The Scouter2 Authors.
 *
 *  @https://github.com/scouter-project/scouter2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package scouter2.collector.legacy.util;

import lombok.val;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 2019-07-29
 */
public class XmlUtil {

    private static final String TRANSFORMER_FACTORY_IMPL
            = "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl";

    public static void writeXmlFileWithIndent(Document doc, File file, int indent) {
        try {
            val transformerFactory = TransformerFactory
                    .newInstance(TRANSFORMER_FACTORY_IMPL, XmlUtil.class.getClassLoader());

            val transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", String.valueOf(indent));
            val source = new DOMSource(doc);
            val result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
