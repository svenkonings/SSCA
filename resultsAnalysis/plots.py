import matplotlib.pyplot as plt
import pandas as pd


def to_color(faults):
    if faults == 0:
        return 'blue'
    else:
        return 'red'


def plot(path, name, x_axis, y_axis):
    df = pd.read_csv(path)
    df = df.round({x_axis: 1, y_axis: 1})
    df['faults'] = df['faults'].apply(to_color)
    df = df.groupby([x_axis, y_axis, 'faults']).size().reset_index(name='count')
    df.plot.scatter(x_axis, y_axis, c=df['faults'], s=df['count'], alpha=0.5)
    plt.xlabel('Functional score')
    plt.ylabel('Imperative score')
    plt.title(name)
    plt.savefig('fig/' + name + '.pdf', bbox_inches='tight')
    # plt.show()
    plt.close()


projects = [
    ('akka', 'Akka'),
    ('coursier', 'Coursier'),
    ('elastic4s', 'elastic4s'),
    ('gitbucket', 'Gitbucket'),
    ('http4s', 'Http4s'),
    ('lagom', 'Lagom'),
    ('playframework', 'Play framework'),
    ('quill', 'Quill'),
    ('scalafmt', 'scalafmt'),
    ('scala-js', 'Scala.js'),
    ('scio', 'Scio'),
    ('shapeless', 'Shapeless'),
    ('slick', 'Slick'),
    ('zio', 'ZIO'),
]

for path, name in projects:
    plot('../../ScalaMetrics/target/' + path + '/functionResultsBriand.csv', name + " functions",
         'FunctionalScoreFraction', 'ImperativeScoreFraction')
    plot('../../ScalaMetrics/target/' + path + '/objectResultsBriand.csv', name + " objects",
         'FunctionalScoreFractionAvr', 'ImperativeScoreFractionAvr')
