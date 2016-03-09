import java.security.PrivateKey
import java.security.SecureRandom
import java.security.KeyPairGenerator
import java.security.KeyPair
import java.security.PublicKey
import java.security.PrivateKey
import java.security.Signature

// http://www.java2ee.ru/crypt/crypt03.html

SecureRandom secrand = new SecureRandom();
//Byte [] b = new byte [20];
// Заполнение массива случайным набором битов
//secrand.setSeed (b);
//println b

/*
byte [] randomBytes = new byte [64];
secrand.nextBytes (randomBytes);
println randomBytes
*/

KeyPairGenerator keygen = KeyPairGenerator.getInstance("DSA");
keygen.initialize (1024, secrand);


KeyPair keys = keygen.generateKeyPair();

PublicKey pubkey = keys.getPublic();
PrivateKey privkey = keys.getPrivate();

//Чтобы подписать сообщение создается объект Signature, реализующий алгоритм подписи.
Signature sigalg = Signature.getInstance("DSA");

// Данный объект может использоваться как для подписи, так и для верификации сообщения. Чтобы подготовить объект к подписанию сообщения, необходимо вызвать метод initSign () и передать ему закрытый ключ.
sigalg.initSign(privkey);

// Теперь, используя метод update (), нужно передать байты объекту Signature так же, как это делалось с дайджестом сообщения.
byte [] bytes = "some string".getBytes();
sigalg.update(bytes);

// И, наконец, используя метод sign (), нужно вычислить подпись. Она представляется в виде байтового массива.
byte [] signature = sigalg.sign();

println signature

// Verify
// Получатель сообщения должен получить объект, реализующий алгоритм подписи DSA, и вызвать метод initVerify (), передав ему в качестве параметра открытый ключ.
Signature verifyalg = Signature.getInstance("DSA");
verifyalg.initVerify(pubkey);

// После этого сообщение следует передать объекту Signature.
// byte [] bytes = . . .;
bytes = "some string!".getBytes();;
verifyalg.update(bytes);

// Теперь подпись можно проверить.
boolean check = verifyalg.verify (signature);
println check