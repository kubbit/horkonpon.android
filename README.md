# HorKonpon

HorKonpon is a mobile app to report problems or suggest improvements to your local council. It uses **GPS** to detect your **current location** and you can **take a picture** or type text to describe your problem.

This information is then sent (in `JSON` format) to a central server which redirects the information to the corresponding council in the most appropiate way:
- As an `e-mail` with all information (**satellite images** of the location, **picture**, ...)
- As `JSON`/`XML`/`other` to integrate with third party software
- ...

Visit [Kubbit](http://kubbit.com/horkonpon/) to learn more *(currently only in spanish)*.

## Download
This application is available in [Google Play](https://play.google.com/store/apps/details?id=com.kubbit.horkonpon) and [App Store](https://itunes.apple.com/es/app/horkonpon/id860821031?mt=8).

## Platforms
This is the repository for `Android` platform. For the `iOS` one visit [HorKonpon for iOS](https://github.com/kubbit/horkonpon.ios)

## Public API
It can be integrated with third party software using `JSON`. This is an example of message sent to the server:

```json
{
	"version": 2,
	"date": "2014-09-22T08:35:47",
	"gps":
	{
		"latitude": 43.321259,
		"longitude": -1.981787,
		"accuracy": 10
	},
	"file":
	{
		"filename": "picture1.jpg",
		"content": "<content in base64>"
	},
	"address":
	{
		"locality": "Donostia - San Sebastian",
		"address": "Gipuzkoa Plaza, s/n"
	},
	"user":
	{
		"fullname": "My Name",
		"mail": "citizen@example.org",
		"notify": true,
		"language": "en"
	},
	"comments": "This is a test."
}
```

And this is an example response:
```json
{
	"version": 2,
	"status": 0,
	"code": 46401,
	"message": "Thanks, your report will be investigated shortly."
}
```

## License
This program is distributed under GNU General Public License version 2 (GPLv2).
