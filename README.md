# SoundScribe

<img src="soundscribe-ui/static/logo2.png" width="150" >

SoundScribe performs automatic music transcription, visualizes gathered data and enables user to modify the results.

### Front-end
See [soundscribe-ui/README.md](soundscribe-ui/README.md)

### Back-end
See [soundscribe-be/README.md](soundscribe-be/README.md)

### Docker
To run whole app with all components on localhost, run:
```bash
docker-compose up -d
```

Available services are:
* `localhost` - UI app, running on nuxt.js
* `localhost/be` - BE server
* `localhost/auth` - authorization server panel

> **Note** - there is no available script that allows you to change host
instantly. To do that you have to change it on every container manually.

### References
~~~
@thesis{ key,
  author = "Mateusz Lamecki \and Mikołaj Nogalski \and Krystian Nowakowski \and Bartosz Osipiuk",
  title = "{Rozbudowa modułu pozyskiwania, prezentacji i edycji melodii jednogłosowej muzyki
  tradycyjnej współpracującego z biblioteką cyfrową dLibra}",
  school = "Poznan University of Technology",
  address = "Pozna{\’n}, Poland",
  year = "2020",
}
~~~
