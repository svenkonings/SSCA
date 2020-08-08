import matplotlib.colors
import matplotlib.pyplot as plt
import pandas as pd


def to_color(faults):
    if faults == 0:
        return 'blue'
    else:
        return 'red'


def scatter_faults(path, name, x_axis, y_axis):
    df = pd.read_csv(path)
    df = df.round({x_axis: 1, y_axis: 1})
    df['faults'] = df['faults'].apply(to_color)
    df = df.groupby([x_axis, y_axis, 'faults']).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, c=df['faults'], s=df['count'], alpha=0.5)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    plt.savefig('fig/scatter-faults/' + name + '.pdf', bbox_inches='tight')
    # plt.show()
    plt.close()


def scatter(path, name, x_axis, y_axis):
    df = pd.read_csv(path)
    df = df.round({x_axis: 1, y_axis: 1})
    df = df.groupby([x_axis, y_axis, ]).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, s=df['count'], alpha=0.5)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    plt.savefig('fig/scatter/' + name + '.pdf', bbox_inches='tight')
    # plt.show()
    plt.close()


def scatter_color(path, name, x_axis, y_axis):
    df = pd.read_csv(path)
    df = df.round({x_axis: 1, y_axis: 1})
    df = df.groupby([x_axis, y_axis, ]).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, c=df['count'], cmap="RdYlGn_r", alpha=0.5, norm=matplotlib.colors.LogNorm(),
                    edgecolors='none')
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    plt.savefig('fig/scatter-color/' + name + '.pdf', bbox_inches='tight')
    # plt.show()
    plt.close()


def hist_faults(path, name, axis):
    df = pd.read_csv(path)
    non_faulty = df[df['faults'] == 0]
    faulty = df[df['faults'] > 0]
    plt.hist([faulty[axis], non_faulty[axis]], bins=10, stacked=True, color=['lightcoral','darkseagreen'])
    plt.xlabel('Paradigm score')
    plt.ylabel('Occurrences')
    plt.title(name)
    plt.savefig('fig/hist-faults/' + name + '.pdf', bbox_inches='tight')
    plt.show()
    plt.close()


projects = [
    ('akka', 'Akka'),
    ('coursier', 'Coursier'),
    ('gitbucket', 'Gitbucket'),
    ('http4s', 'Http4s'),
    ('lagom', 'Lagom'),
    ('quill', 'Quill'),
    ('scalafmt', 'scalafmt'),
    ('scala-js', 'Scala.js'),
    ('scio', 'Scio'),
    ('shapeless', 'Shapeless'),
    ('slick', 'Slick'),
    ('zio', 'ZIO'),
]

for path, name in projects:
    # scatter('../../ScalaMetrics/target/' + path + '/functionResultsBriand.csv', name + " functions",
    #         'FunctionalScoreFraction', 'ImperativeScoreFraction')
    # scatter('../../ScalaMetrics/target/' + path + '/objectResultsBriand.csv', name + " objects",
    #         'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    # scatter_faults('../../ScalaMetrics/target/' + path + '/functionResultsBriand.csv', name + " functions",
    #                'FunctionalScoreFraction', 'ImperativeScoreFraction')
    # scatter_faults('../../ScalaMetrics/target/' + path + '/objectResultsBriand.csv', name + " objects",
    #                'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    # scatter_color('../../ScalaMetrics/target/' + path + '/functionResultsBriand.csv', name + " functions",
    #               'FunctionalScoreFraction', 'ImperativeScoreFraction')
    # scatter_color('../../ScalaMetrics/target/' + path + '/objectResultsBriand.csv', name + " objects",
    #               'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
    hist_faults('../../ScalaMetrics/target/' + path + '/functionResultsBriand.csv', name + " functions",
                  'ParadigmScoreFraction')
    hist_faults('../../ScalaMetrics/target/' + path + '/objectResultsBriand.csv', name + " objects",
                  'ParadigmScoreFractionAvr')
