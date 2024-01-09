import socketserver
from http.server import BaseHTTPRequestHandler
from urllib import parse
import gensim
import fasttext
import shared_vars as vars
from numpy import dot
from numpy.linalg import norm

print('loading word vector model...')
#model = gensim.models.fasttext.load_facebook_vectors(vars.WORD_VECS_PATH, encoding='utf-8')
model = fasttext.load_model(vars.WORD_VECS_PATH)
#model = gensim.models.KeyedVectors.load_word2vec_format(vars.WORD_VECS_PATH, encoding='utf-8', binary=True)

def cos_sim(a,b):
    return dot(a,b)/(norm(a)*norm(b))
   

class WVServer(BaseHTTPRequestHandler):

    def _set_headers(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()

    def do_GET(self):
        q = dict(parse.parse_qsl(parse.urlsplit(self.path).query))
        delim = 'MAKE_LIST'
        p1, p2 = q['p1'], q['p2']
        if p1.startswith(delim):
            p1 = p1.replace(delim, '')
            p1 = p1.split(' ')
        if p2.startswith(delim):
            p2 = p2.replace(delim, '')
            p2 = p2.split(' ')
        #res = getattr(model, q['f'])(p1, p2)
        res = cos_sim(model.get_sentence_vector(' '.join(p1)), model.get_sentence_vector(' '.join(p2)))
        self._set_headers()
        self.wfile.write(str(res).encode("utf8"))
    def log_message(self, format, *args):
        return


PORT = 8000
try:
    print(f'w2v listening on {PORT}')
    httpd = socketserver.TCPServer(("", PORT), WVServer)
    httpd.serve_forever()
except KeyboardInterrupt:
    httpd.shutdown()
    print('server shut down')
