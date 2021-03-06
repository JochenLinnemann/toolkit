/*
 * Copyright (c) 1998-2017 by Richard A. Wilkes. All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, version 2.0. If a copy of the MPL was not distributed with
 * this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is "Incompatible With Secondary Licenses", as
 * defined by the Mozilla Public License, version 2.0.
 */

package com.trollworks.toolkit.expression.function;

import com.trollworks.toolkit.annotation.Localize;
import com.trollworks.toolkit.expression.ArgumentTokenizer;
import com.trollworks.toolkit.expression.EvaluationException;
import com.trollworks.toolkit.expression.Evaluator;
import com.trollworks.toolkit.utility.Localization;

public class Max implements ExpressionFunction {
	@Localize("Two numeric arguments are required")
	@Localize(locale = "pt-BR", value = "Dois argumentos numéricos são requeridos")
	private static String INVALID_ARGUMENTS;

	static {
		Localization.initialize();
	}

	@Override
	public final String getName() {
		return "max"; //$NON-NLS-1$
	}

	@Override
	public final Object execute(Evaluator evaluator, String arguments) throws EvaluationException {
		try {
			Evaluator ev = new Evaluator(evaluator);
			ArgumentTokenizer tokenizer = new ArgumentTokenizer(arguments);
			double arg1 = ArgumentTokenizer.getDouble(ev.evaluate(tokenizer.nextToken()));
			double arg2 = ArgumentTokenizer.getDouble(ev.evaluate(tokenizer.nextToken()));
			return Double.valueOf(Math.max(arg1, arg2));
		} catch (Exception exception) {
			throw new EvaluationException(INVALID_ARGUMENTS, exception);
		}
	}
}
