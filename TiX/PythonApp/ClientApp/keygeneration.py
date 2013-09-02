from Crypto.PublicKey import RSA
from Crypto import Random


def generateKeyPair(privateKeyFile, publicKeyFile):
	keys = RSA.generate(1024)
	exportableKeys = []
	privHandle = open(privateKeyFile, 'wb')
	exportableKeys.append(keys.exportKey())
	privHandle.write(keys.exportKey())
	privHandle.close()

	pubHandle = open(publicKeyFile, 'wb')
	pubHandle.write(keys.publickey().exportKey())
	exportableKeys.append(keys.publickey().exportKey())
	pubHandle.close()

	return exportableKeys[1]

if __name__ == "__main__":
#	generateKeyPair("/etc/TIX/tix_key.priv", "/etc/TIX/tix_key.pub")


	generateKeyPair("tix_key.priv", "tix_key.pub")
	privateKeyFile = open('tix_key.priv','r')
	publicKeyFile = open('tix_key.pub','r')
	privateKey = RSA.importKey(privateKeyFile.read())
	publicKey = RSA.importKey(publicKeyFile.read())
	msg = "hola como estas"
	signedMessage = privateKey.sign(msg, Random.new().read) #El cliente firma el msg
	print signedMessage

	publicKeyPlain = publicKey.exportKey()
	#Cliente envia al server: DATA|publicKeyPlain|signedMeessage|msg

	## En el server, valido el 'DATA' y con la signature y el msg lo verifico. Por ultimo uso la publicKeyPlain para buscar el usuario

<<<<<<< HEAD
	print publicKey.verify(msg, signedMessage) # En el servidor se hace el VERIFY, para esto se necesita tambien la firma!

=======
	client_pub_key = RSA.importKey(publicKeyPlain) # import pub key from string

	print client_pub_key.verify(msg, signedMessage) # En el servidor se hace el VERIFY, para esto se necesita tambien la firma!
>>>>>>> c5ff98dc930c83822aabe236952d0c8c27761cea