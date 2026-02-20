# Simple Cloudflare DDNS Client/Updater

### Why am I publishing this?
Because I couldn't find anything simple to set up Cloudflare as a kind of DDNS without overkill, and I want others to have a good and simple-to-use resource for doing just that.

> [!NOTE]
> I am not planning to actively maintain this for a while thanks to exam stress. I might start doing so in a few weeks, but for now, everything is working as intended.
> (This does **not** mean I won't try to fix reported bugs, but I likely won't add new features for now.)
>
> Also, this is more of a "Works on my machine" thing, I can't guarantee it will work on yours too.
>
>
> That being said, I hope it will work for you with less trouble than with other software!

## Installation

1. Download the JAR from the Releases Tab
2. Get your Zone ID from Cloudflare. See [here](https://developers.cloudflare.com/fundamentals/account/find-account-and-zone-ids/#copy-your-zone-id) for more.
3. Get a token for the Cloudflare API. See [here](https://developers.cloudflare.com/fundamentals/api/get-started/create-token/) for more.
4. Write your configuration file. See [Configuration](#Configuration) for more.
5. Run the JAR: `java -jar path/to/jar path/to/config.json`

Once finished, you can also set the above command to run periodically using `cron`-jobs, Task Scheduler or any other tool.

## Dependencies
| Tool | Why does it need to be installed? | How to check for installation |
| --- | --- | --- |
| `curl` | Curl needs to be installed in order to check the public IP address for both IPv4 and IPv6 <small>because Java is not able to let the developer decide which one to use when</small>. | `curl -v` |
| `java` | Java needs to be installed in order to run this tool. | `java --version` |

## Configuration
### Overview
The config file is written in JSON and consists of the `zoneId`, `apiToken` and `records` tag.

The first two are Strings, and, of course, contain your Zone ID and API Token (**Remove any spaces in the strings!**).

The `records` tag is an Array of individual objects, each one containing an `ipv` tag and the `recordId` tag.

`ipv` can have the value `4` to update the record to your public IPv4 address, or `6` to update it with your public IPv6 address.

`recordId` is the ID of the DNS Record. You can get the ID of your DNS Records with the following command (requires `jq`):
``` bash
curl "https://api.cloudflare.com/clients/v4/zones/<ZONE_ID>/dns_records" -H "Authorization: Bearer <API_TOKEN>" | jq .result
```

### Example
<details>
  <summary>Example Configuration</summary>

``` json
{
  "apiToken": "1234567890ABCDEF123456",
  "zoneId": "1234567890ABCDEF",

  "records": [
    {"ipv": "4", "recordId": "1234567890"},
    {"ipv": "4", "recordId": "1234567890"},
    {"ipv": "6", "recordId": "1234567890"},
    {"ipv": "6", "recordId": "1234567890"}
  ]
}
```

</details>
