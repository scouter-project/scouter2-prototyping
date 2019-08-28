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
package scouter2.collector.transport.legacy.service;

import lombok.extern.slf4j.Slf4j;
import scouter.io.DataInputX;
import scouter.io.DataOutputX;
import scouter.util.scan.Scanner;
import scouter2.collector.common.log.ThrottleConfig;
import scouter2.collector.transport.legacy.service.annotation.LegacyServiceHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class LegacyServiceHandlingProxy {
	protected static HashMap<String, Invocation> handlers = new HashMap<String, Invocation>();
	protected static class Invocation {
		public static final ThrottleConfig S_0015 = ThrottleConfig.of("S0015");
		Object object;
		Method method;
		public Invocation(Object object, Method method) {
			this.object = object;
			this.method = method;
		}
		public void exec(DataInputX in, DataOutputX out, boolean login) {
			try {
				method.invoke(object, new Object[] { in, out, login });
			} catch (InvocationTargetException t) {
				if (t.getCause() != null) {
					t.getCause().printStackTrace();
				} else {
					t.printStackTrace();
				}
			} catch (Exception e) {
				log.error(e.getMessage(), S_0015, e);
			}
		}
		@Override
		public String toString() {
			return object.getClass().getName() + "." + method.getName();
		}
	}

	public static void load() {
		String pkg = Scanner.cutOutLast(LegacyServiceHandlingProxy.class.getName(), ".");
		Set<String> classes = new Scanner(pkg).process(LegacyServiceHandlingProxy.class.getClassLoader());
		Set<String> custom = new Scanner(System.getProperty("scouter.handler")).process();
		classes.addAll(custom);
		Iterator<String> itr = classes.iterator();
		while (itr.hasNext()) {
			try {
				Class c = Class.forName(itr.next());
				if (Modifier.isPublic(c.getModifiers()) == false) {
					continue;
				}
				try {
					Method[] m = c.getDeclaredMethods();
					for (int i = 0; i < m.length; i++) {
						LegacyServiceHandler mapAn = (LegacyServiceHandler) m[i].getAnnotation(LegacyServiceHandler.class);
						if (mapAn == null)
							continue;
						String key = mapAn.value();
						Invocation news = new Invocation(c.newInstance(), m[i]);
						Invocation olds = handlers.get(key);
						if (olds != null) {
							log.warn("Warning duplicated Handler key=" + key + " old=" + olds + " new=" + news);
						}
						handlers.put(key, news);
					}
				} catch (Exception x) {
					x.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void process(String cmd, DataInputX in, DataOutputX out, boolean login) {
		Invocation handler = handlers.get(cmd);
		if (handler != null) {
			handler.exec(in, out, login);
		} else {
			throw new RuntimeException("Non implemented Legacy CMD: " + cmd);
		}
	}
}
