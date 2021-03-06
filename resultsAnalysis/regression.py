import matplotlib.pyplot as plt
import numpy as np
import math
import pandas as pd
import statsmodels.api as sm
import statsmodels.formula.api as smf
from sklearn import linear_model

def forward_selected(data, response, op):
    remaining = set(data.columns)
    remaining.remove(response)
    selected = []
    current_score, best_new_score = float('Inf'), float('Inf')
    running = True
    while remaining and running:
        scores_with_candidates = []
        for candidate in remaining:
            formula = "{} ~ {} + 1".format(response,
                                           ' + '.join(selected + [candidate]))
            try:
                score = abs(op(formula, data, missing='drop').fit().aic)
            except Exception as e:
                score = float('Inf')

            scores_with_candidates.append((score, candidate))

        scores_with_candidates.sort()
        scores_with_candidates.reverse()
        best_new_score, best_candidate = scores_with_candidates.pop()

        if current_score > best_new_score:
            remaining.remove(best_candidate)
            selected.append(best_candidate)
            current_score = best_new_score
        else:
        	running = False

    formula = "{} ~ {} + 1".format(response,
                                   ' + '.join(selected))

    return formula

def forward_selected_al(data, response, op):
    remaining = set(data.columns)
    remaining.remove(response)
    selected = []
    current_score, best_new_score = 0.0, 0.0
    running = True
    while remaining and running:
        scores_with_candidates = []
        for candidate in remaining:
            formula = "{} ~ {} + 1".format(response,
                                           ' + '.join(selected + [candidate]))
            try:
                score = op(formula, data, missing='drop').fit().prsquared

            except Exception as e:
                score = 0.0

            scores_with_candidates.append((score, candidate))

        scores_with_candidates.sort()
        best_new_score, best_candidate = scores_with_candidates.pop()

        print current_score, best_new_score
        if current_score < best_new_score:
            remaining.remove(best_candidate)
            selected.append(best_candidate)
            current_score = best_new_score
        else:
        	running = False

    formula = "{} ~ {} + 1".format(response,
                                   ' + '.join(selected))

    model = op(formula, data, missing='drop').fit()
    return model

def forward_selected_rev(data, response, op):
    remaining = list(set(data.columns))
    remaining.remove(response)
    selected = []
    current_score, best_new_score = float('Inf'), float('Inf')
    running = True
    while remaining and running:
        scores_with_candidates = []
        for candidate in remaining:
            formula = "{} ~ {} + 1".format(response,
                                           ' + '.join(selected + [candidate]))
            try:
                score = abs(op(formula, data, missing='drop').fit().aic)
            except Exception as e:
                score = float('Inf')

            scores_with_candidates.append((score, candidate))

        scores_without_candidates = []
        for candidate in selected:
            selected_tmp = selected[:]
            selected_tmp.remove(candidate)

            formula = "{} ~ {} + 1".format(response,
                                           ' + '.join(selected_tmp))
            try:
                score = abs(op(formula, data, missing='drop').fit().aic)
            except Exception as e:
                score = float('Inf')

            scores_without_candidates.append((score, candidate))

        scores_with_candidates.sort()
        scores_with_candidates.reverse()

        scores_without_candidates.sort()
        scores_without_candidates.reverse()

        best_new_score1, best_candidate1 = scores_with_candidates.pop()

        if scores_without_candidates:
            best_new_score2, best_candidate2 = scores_without_candidates.pop()

            if best_new_score1 < best_new_score2:
                best_new_score = best_new_score1
                best_candidate = best_candidate1
                list1 = remaining
                list2 = selected
            else:
                best_new_score = best_new_score2
                best_candidate = best_candidate2
                list1 = selected
                list2 = remaining
        else:
            best_new_score = best_new_score1
            best_candidate = best_candidate1
            list1 = remaining
            list2 = selected

        if current_score > best_new_score:
            list1.remove(best_candidate)
            list2.append(best_candidate)
            current_score = best_new_score
        else:
            running = False

    formula = "{} ~ {} + 1".format(response,
                                   ' + '.join(selected))

    model = op(formula, data, missing='drop').fit()
    return model

def correctness(predTable):
	if (predTable[0, 1] + predTable[1, 1]) > 0.0:
		return predTable[1, 1] / (predTable[0, 1] + predTable[1, 1])
	return 0.0

def completeness(predTable):
	if (predTable[2, 0] + predTable[2, 1]) > 0.0:
		return predTable[2, 1] / (predTable[2, 0] + predTable[2, 1])
	return 0.0


def genPredTable(result, df, x_label, y_label, dep, threshold=0.5):
	predictions = map(lambda x: 1 if x > threshold else 0, result.predict(sm.add_constant(df[x_label])))
	real_pred = np.vstack((df[y_label], df[dep], predictions)).T

	nofaultNofault = len(filter(lambda x: x[1] == 0 and x[2] == 0, real_pred))
	nofaultFault = len(filter(lambda x: x[1] == 0 and x[2] > 0, real_pred))
	faultNofault = len(filter(lambda x: x[1] > 0 and x[2] == 0, real_pred))
	faultFault = len(filter(lambda x: x[1] > 0 and x[2] > 0, real_pred))



	faultsNofaults = np.asarray(filter(lambda x: x[1] > 0 and x[2] == 0, real_pred))
	faultsFaults = np.asarray(filter(lambda x: x[1] > 0 and x[2] > 0, real_pred))

	faultsNofaults = sum(faultsNofaults[:, 0]) if len(faultsNofaults > 0) else 0
	faultsFaults = sum(faultsFaults[:, 0]) if len(faultsFaults > 0) else 0

	predTable = np.asarray([[nofaultNofault, nofaultFault], [faultNofault, faultFault], [faultsNofaults, faultsFaults]])
	return predTable
	

def printResultMatrix(result, df, x_label, y_label, dep, threshold=0.5):
	predTable = genPredTable(result, df, x_label, y_label, dep, threshold)

	print "Predicted\tNo Fault\tFault"
	print "Actual"
	print "No Fault\t{}\t\t{}".format(predTable[0, 0], predTable[0, 1])
	print "Fault\t\t{} ({})\t\t{} ({})".format(predTable[1, 0], predTable[2, 0], predTable[1, 1], predTable[2, 1])
	print ""
	print "Completeness: {}".format(completeness(predTable.astype(float)))
	print "Correctness: {}".format(correctness(predTable.astype(float)))
	print ""

def createComCorGraph(result, df, x_label, y_label, dep):
	fig, ax = plt.subplots()
	xpts = np.linspace(0, 1, 100)

	comp = []
	corr = []
	for x in xpts:
		predTable = genPredTable(result, df, x_label, y_label, dep, x)
		comp.append(completeness(predTable.astype(float)))
		corr.append(correctness(predTable.astype(float)))

	ax.plot(xpts, comp, "-r", label="completeness")
	ax.plot(xpts, corr, "-b", label="correctness")
	ax.grid(True)
	ax.set_xlabel('Threshold $\pi$')
	ax.set_ylabel('Percentage %')
	legend = ax.legend(loc='upper left')
	return fig, ax

def createComCorGraphData(comp, corr):
    fig, ax = plt.subplots()
    xpts = np.linspace(0, 1, len(comp))

    ax.plot(xpts, comp, "-r", label="completeness")
    ax.plot(xpts, corr, "-b", label="correctness")
    ax.grid(True)
    ax.set_xlabel('Threshold $\pi$')
    ax.set_ylabel('Percentage %')
    legend = ax.legend(loc='upper left')
    return fig, ax


def plotLogisticRegression(df, result, x_label, y_label):
	fig, ax = plt.subplots()


	xpts = np.linspace(0, df[x_label].max(), 1000).reshape((-1, 1))
	ax.plot(xpts, result.predict(sm.add_constant(xpts)))

	ax.plot(df[x_label], df[y_label], "o")

	return fig, ax


def logitRegression(x, y):
	module = sm.Logit(y, sm.add_constant(x))
	return module.fit()


def olsRegression(x, y):
	module = sm.OLS(y, sm.add_constant(x))
	return module.fit()



if __name__ == '__main__':	
	def wavg(group):
		r = []
		for x in group.keys():
			if str(x) == b:
				r.append(group[b].sum())
			elif x == "path" or x == "commit":
				r.append(group[x])
			else:
				r.append(group[x].mean())
		return pd.Series(r, index=group.keys())

	df = pd.concat(pd.read_csv("fullOutputSHA.csv", usecols=[1, 2, 7], chunksize=1000, iterator=True), ignore_index=True)

	a = "WMCcc"
	b = "faults"


	# df = df.groupby(['path']).apply(wavg)
	df[b] = df[b].map(lambda x: 1 if x > 0.0 else 0)


	result = logitRegression(df[a], df[b])
	print result.summary()

	printResultMatrix(result, df, a, b)

	plotLogisticRegression(df, result, a, b)
	plt.show()
