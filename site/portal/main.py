import flask


app = flask.Flask(__name__, static_folder='static', template_folder='html')



@app.route('/')
def index():
	return flask.render_template('index.html')
