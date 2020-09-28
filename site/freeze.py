from flask_frozen import Freezer
from portal.main import app

app.config['FREEZER_DESTINATION'] = '../../docs'
app.config['FREEZER_DESTINATION_IGNORE'] = ['CNAME','.nojekyll','notes']

freezer = Freezer(app)

if __name__ == '__main__':
    freezer.freeze()

