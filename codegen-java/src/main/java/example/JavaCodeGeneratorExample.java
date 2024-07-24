/**
 * Copyright © 2024 Apple Inc. and the Pkl project authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example;

import org.pkl.config.java.Config;
import org.pkl.config.java.ConfigEvaluator;

import org.pkl.config.java.mapper.ValueMapperBuilder;
import org.pkl.core.Evaluator;
import org.pkl.core.ModuleSource;
import org.pkl.core.PModule;

public class JavaCodeGeneratorExample {
  public static void main(String[] args) {
    PModule config;
    try(var evaluator = Evaluator.preconfigured()) {
      config = evaluator.evaluate(ModuleSource.modulePath("/Repro.pkl"));
    }

    var mapper = ValueMapperBuilder.preconfigured().build();
    var module = config.get("list");
    var list = mapper.map(module, Repro.As.class);
    System.out.println(list);
  }
}
