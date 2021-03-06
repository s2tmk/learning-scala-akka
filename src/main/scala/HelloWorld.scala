import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}

import scala.io.StdIn

object HttpServer {

  def main(args: Array[String]): Unit = {

    implicit val system = ActorSystem(Behaviors.empty, "http-server")

    implicit val executionContext = system.executionContext

    val route =
      path("hello") {
        get {
          complete(HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            "<h1>Hello, this is http-server</h1>"
          ))
        }
      }

    val host = "localhost"
    val port = 8080

    val bindingFuture = Http().newServerAt(host, port).bind(route)

    println(s"Server online at http://localhost:8080/hello\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind()) // port をアンバインド
      .onComplete(_ => system.terminate()) // system を停止
  }
}

// アクターに送信するメッセージ用のクラス。
// StringやIntのようなプリミティブな型を直接送信することもできるが、
// 以下のようにメッセージ用のcase classを作って送信するのが常套手段。
final case class Hello(name: String)

// アクターの振る舞いを定義したオブジェクト。
// 以前はActorトレイトを継承してreceiveメソッドを実装していたが、
// 2.6からはプレーンなオブジェクトになった。
object PrintHello {

  // メッセージを受信した際の｢振る舞い｣を定義する。
  // ※Behaviorを返すならなんでもいいはずですが、慣例的にappyメソッドで定義することが多いようです。
  def apply(): Behavior[Hello] = Behaviors.receiveMessage { message =>
    // 受け取ったメッセージを標準出力。
    println(s"Hello ${message.name}!")

    // 次にメッセージを受信したときの振る舞いを返す。
    // 今回は何度呼ばれても同じ動きをしたいので、Behaviors.sameを使う。
    Behaviors.same
  }
}

// アクター生成･メッセージ送信処理
object Main extends App {

  // 今回作るアクターの｢振る舞い｣を定義。
  // PrintHello()はPrintHello.apply()の略記法。
  val behavior: Behavior[Hello] = PrintHello()
  // 今回作るアクターの名前。
  val name = "PrintHelloMain"

  // 指定した｢振る舞い｣と名前を持つアクターを生成。
  val printHelloActor: ActorSystem[Hello] = ActorSystem(behavior, name)

  // 送信するメッセージの作成。
  val message = Hello("World")

  // printHelloActorにメッセージを送信する。
  // ｢!｣メソッド(Bangと読むらしい)はtellメソッドのシンタックスシュガー。
  // 以下はprintHelloActor.tell(message)と同じ。
  printHelloActor ! message
}
