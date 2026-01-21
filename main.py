from __future__ import annotations
import customtkinter
import threading
import os
import re
import sys
import subprocess
import socket
from tkinter import filedialog
from typing import List, Dict, Any, Callable, Optional

import yt_dlp
from yt_dlp.utils import DownloadError

# --- 3. SERVICE LAYER ---
# Handles all backend logic and is completely independent of the UI.

class DownloaderService:
    """
    Handles all interactions with yt-dlp, implementing the Session Emulation paradigm.
    """
    @staticmethod
    def _get_base_ydl_options(logger: 'YtdlpLogger', browser: Optional[str] = None) -> Dict[str, Any]:
        """
        Creates the expert-level base dictionary of yt-dlp options for session
        emulation and maximum network resilience.
        """
        opts = {
            'logger': logger,
            'verbose': True,
            'nocheckcertificate': True,
            'force_ipv4': True,
           
            # --- Network Resilience ---
            'retries': 15,
            'fragment_retries': 15,
            'skip_unavailable_fragments': True,
            'socket_timeout': 45,

            # --- Throttling Mitigation ---
            'sleep_interval': 2,
            'max_sleep_interval': 8,

            # --- Geo-Restriction Bypass ---
            'geo_bypass': True,
           
            # --- Format Selection ---
            'format': 'bestvideo*+bestaudio/best',
        }
       
        # --- Authentication: The most critical component ---
        if browser and browser.lower() != 'none':
            opts['cookies-from-browser'] = (browser.lower(),)
       
        return opts

    @staticmethod
    def sanitize_filename(filename: str) -> str:
        """Cleans a string to create a valid and meaningful filename."""
        if not filename: return "downloaded_file"
        if "Direct Stream Link" in filename:
            return "downloaded_stream"
        parts = re.split(r'\s*[|｜·]\s*', filename)
        clean_filename = max(parts, key=len, default='')
        clean_filename = re.sub(r'\[.*?\]|\(.*?\)', '', clean_filename).strip()
        clean_filename = re.sub(r'[\\/:*?"<>|]', '_', clean_filename)
        clean_filename = re.sub(r'\s+', ' ', clean_filename).strip(' ._')
        return clean_filename[:150] if clean_filename else "downloaded_video"

    def get_video_info(self, urls: List[str], browser: Optional[str], logger: 'YtdlpLogger') -> List[Dict[str, str]]:
        """Fetches metadata for a list of URLs using an emulated session."""
        video_data = []
        ydl_opts = self._get_base_ydl_options(logger, browser)
        ydl_opts.update({
            'quiet': True,
            'extract_flat': True,
        })

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            for url in urls:
                if '.m3u8' in url:
                    video_data.append({'title': f"Direct Stream: {os.path.basename(url)}", 'url': url})
                    continue
                try:
                    info = ydl.extract_info(url, download=False)
                    if 'entries' in info:
                        for entry in info.get('entries', []):
                            if entry: video_data.append({'title': entry.get('title', 'Untitled'), 'url': entry.get('url', '')})
                    else:
                        video_data.append({'title': info.get('title', 'Untitled'), 'url': info.get('webpage_url', url)})
                except DownloadError as e:
                    logger.error(f"Could not fetch info for {url}: {e}")
        return video_data

    def download_videos(self, urls: List[str], browser: Optional[str], dl_options: Dict[str, Any], logger: 'YtdlpLogger'):
        """Downloads videos using the specified options and an emulated session."""
        ydl_opts = self._get_base_ydl_options(logger, browser)
        ydl_opts.update(dl_options)

        with yt_dlp.YoutubeDL(ydl_opts) as ydl:
            ydl.download(urls)

# --- 1. VIEW LAYER ---
# Manages all GUI elements and is controlled by the App class.

class UIManager:
    """Manages all customtkinter widgets and UI updates for the main app."""
    QUALITY_OPTIONS = ["Best", "1080p", "720p", "480p", "360p"]
    BROWSER_OPTIONS = ["None", "Chrome", "Firefox", "Edge", "Vivaldi", "Brave"]

    def __init__(self, app: customtkinter.CTk, callbacks: Dict[str, Callable]):
        self.app = app
        self.app.title("Universal Video Downloader 5.2")
        self.app.geometry("1100x750")
        self.app.minsize(1050, 700)
        self.app.grid_columnconfigure(0, weight=1)
        self.app.grid_rowconfigure(1, weight=1)

        # Create UI variables
        self.download_path = customtkinter.StringVar(value=self._get_default_download_path())
        self.status_message = customtkinter.StringVar(value="Ready")
        self.audio_format_var = customtkinter.StringVar(value="none")
        self.quality_var = customtkinter.StringVar(value=self.QUALITY_OPTIONS[0])
        self.browser_cookie_var = customtkinter.StringVar(value=self.BROWSER_OPTIONS[0])
        self.placeholder_text = "Paste your video, playlist, or course links here..."

        self._create_widgets(callbacks)

    def _get_default_download_path(self) -> str:
        downloads_dir = os.path.join(os.path.expanduser('~'), 'Downloads')
        os.makedirs(downloads_dir, exist_ok=True)
        return downloads_dir

    def _create_widgets(self, callbacks: Dict[str, Callable]):
        # Frame for settings (Save Path, Authentication)
        settings_frame = customtkinter.CTkFrame(self.app)
        settings_frame.grid(row=0, column=0, padx=20, pady=(20, 10), sticky="ew")
        settings_frame.grid_columnconfigure(1, weight=1)
       
        customtkinter.CTkLabel(settings_frame, text="Save To:", font=customtkinter.CTkFont(weight="bold")).grid(row=0, column=0, padx=(15, 5), pady=10, sticky="w")
        customtkinter.CTkLabel(settings_frame, textvariable=self.download_path, anchor="w").grid(row=0, column=1, padx=5, pady=10, sticky="ew")
        customtkinter.CTkButton(settings_frame, text="Change Folder", command=callbacks["select_folder"]).grid(row=0, column=2, padx=15, pady=5)

        customtkinter.CTkLabel(settings_frame, text="Authentication:", font=customtkinter.CTkFont(weight="bold")).grid(row=1, column=0, padx=(15, 5), pady=10, sticky="w")
        customtkinter.CTkLabel(settings_frame, text="Use Cookies From Browser (Recommended for YouTube):").grid(row=1, column=1, padx=5, pady=10, sticky="w")
        customtkinter.CTkOptionMenu(settings_frame, variable=self.browser_cookie_var, values=self.BROWSER_OPTIONS).grid(row=1, column=2, padx=15, pady=5)

        # Main Textbox for URLs
        self.url_textbox = customtkinter.CTkTextbox(self.app, corner_radius=8, border_width=2)
        self.url_textbox.grid(row=1, column=0, padx=20, pady=10, sticky="nsew")
        self.url_textbox.bind("<FocusIn>", self._clear_placeholder)
        self.url_textbox.bind("<FocusOut>", self._add_placeholder)
        self._add_placeholder()

        # Controls Frame
        controls_frame = customtkinter.CTkFrame(self.app, fg_color="transparent")
        controls_frame.grid(row=2, column=0, padx=20, pady=(10, 20), sticky="ew")
        controls_frame.grid_columnconfigure(1, weight=1)

        # Left Panel (Actions)
        left_panel = customtkinter.CTkFrame(controls_frame, fg_color="transparent")
        left_panel.grid(row=0, column=0, padx=(0, 10), sticky="ns")
        self.fetch_button = customtkinter.CTkButton(left_panel, text="Fetch Info", command=callbacks["fetch"], height=40)
        self.fetch_button.pack(pady=(0, 5), fill="x")
        self.download_button = customtkinter.CTkButton(left_panel, text="Download", command=callbacks["download"], height=40, font=customtkinter.CTkFont(size=16, weight="bold"))
        self.download_button.pack(fill="x")

        # Center Panel (Status & Progress)
        status_panel = customtkinter.CTkFrame(controls_frame, fg_color="transparent")
        status_panel.grid(row=0, column=1, sticky="nsew", padx=10)
        status_panel.grid_rowconfigure(1, weight=1)
        status_panel.grid_columnconfigure(0, weight=1)
        self.progress_bar = customtkinter.CTkProgressBar(status_panel, corner_radius=8)
        self.progress_bar.set(0)
        self.progress_bar.pack(fill="x", pady=5, ipady=4)
        self.status_label = customtkinter.CTkLabel(status_panel, textvariable=self.status_message, anchor="center", wraplength=400)
        self.status_label.pack(fill="x", expand=True)

        # Right Panel (Options & Utilities)
        right_panel = customtkinter.CTkFrame(controls_frame, fg_color="transparent")
        right_panel.grid(row=0, column=2, padx=(10, 0), sticky="ns")
       
        options_frame = customtkinter.CTkFrame(right_panel)
        options_frame.pack(pady=(0, 10), fill="x")
        customtkinter.CTkLabel(options_frame, text="Video Quality:").pack(padx=15, pady=(5,0), anchor="w")
        self.quality_menu = customtkinter.CTkOptionMenu(options_frame, variable=self.quality_var, values=self.QUALITY_OPTIONS)
        self.quality_menu.pack(padx=15, pady=(0,10), fill="x")
        customtkinter.CTkLabel(options_frame, text="Audio Format:").pack(padx=15, pady=(5,0), anchor="w")
        customtkinter.CTkRadioButton(options_frame, text="None (Video)", variable=self.audio_format_var, value="none").pack(padx=15, pady=5, anchor="w")
        self.audio_radio_mp3 = customtkinter.CTkRadioButton(options_frame, text="MP3", variable=self.audio_format_var, value="mp3")
        self.audio_radio_mp3.pack(padx=15, pady=5, anchor="w")
        self.audio_radio_m4a = customtkinter.CTkRadioButton(options_frame, text="M4A", variable=self.audio_format_var, value="m4a")
        self.audio_radio_m4a.pack(padx=15, pady=(5,10), anchor="w")

        utility_frame = customtkinter.CTkFrame(right_panel, fg_color="transparent")
        utility_frame.pack(fill="x", expand=True)
        customtkinter.CTkButton(utility_frame, text="Update Engine", command=callbacks["update_engine"]).pack(pady=2, fill="x")
        customtkinter.CTkButton(utility_frame, text="Help/FAQ", command=callbacks["show_help"]).pack(pady=2, fill="x")
        customtkinter.CTkButton(utility_frame, text="View Logs", command=callbacks["view_logs"]).pack(pady=2, fill="x")
        customtkinter.CTkButton(utility_frame, text="Start Over", command=callbacks["start_over"]).pack(pady=2, fill="x")

    def _add_placeholder(self, event=None):
        if not self.url_textbox.get("1.0", "end-1c").strip():
            self.url_textbox.configure(text_color="gray")
            self.url_textbox.insert("1.0", self.placeholder_text)

    def _clear_placeholder(self, event=None):
        if self.url_textbox.get("1.0", "end-1c") == self.placeholder_text:
            self.url_textbox.delete("1.0", "end")
            self.url_textbox.configure(text_color=customtkinter.ThemeManager.theme["CTkTextbox"]["text_color"])

    def set_status(self, message: str, is_error: bool = False):
        self.status_message.set(message)
        self.status_label.configure(text_color=("#E74C3C" if is_error else customtkinter.ThemeManager.theme["CTkLabel"]["text_color"]))

    def set_progress(self, value: float): self.progress_bar.set(value)
   
    def toggle_controls(self, enabled: bool):
        state = "normal" if enabled else "disabled"
        self.fetch_button.configure(state=state)
        self.download_button.configure(state=state)

    def notify_ffmpeg_missing(self):
        self.set_status("Warning: FFmpeg not found. 'Best' quality and audio conversion are disabled.", is_error=True)
        new_options = [q for q in self.QUALITY_OPTIONS if q != "Best"]
        self.quality_var.set(new_options[0])
        self.quality_menu.configure(values=new_options)
        self.audio_radio_mp3.configure(state="disabled")
        self.audio_radio_m4a.configure(state="disabled")

# --- UTILITY/HELPER CLASSES ---

class YtdlpLogger:
    def __init__(self, log_callback: Callable[[str], None]): self.log_callback = log_callback
    def debug(self, msg): self.log_callback(f"DEBUG: {msg}\n")
    def info(self, msg): self.log_callback(f"{msg}\n")
    def warning(self, msg): self.log_callback(f"WARNING: {msg}\n")
    def error(self, msg): self.log_callback(f"ERROR: {msg}\n")

class LogViewerToplevel(customtkinter.CTkToplevel):
    def __init__(self, master):
        super().__init__(master)
        self.title("Live Log Viewer"); self.geometry("900x600"); self.protocol("WM_DELETE_WINDOW", self.withdraw)
        self.log_textbox = customtkinter.CTkTextbox(self, state="disabled", font=("Courier New", 12))
        self.log_textbox.pack(expand=True, fill="both", padx=10, pady=10)
    def add_log_message(self, message):
        self.log_textbox.configure(state="normal"); self.log_textbox.insert("end", message); self.log_textbox.see("end"); self.log_textbox.configure(state="disabled")
    def show(self): self.deiconify()

class HelpToplevel(customtkinter.CTkToplevel):
    def __init__(self, master):
        super().__init__(master)
        self.title("Help & Troubleshooting Guide"); self.geometry("950x700"); self.protocol("WM_DELETE_WINDOW", self.withdraw)
        textbox = customtkinter.CTkTextbox(self, wrap="word", font=("Arial", 14)); textbox.pack(expand=True, fill="both", padx=15, pady=15)
        textbox.insert("1.0", """**The New Way to Download: Session Emulation**
This version uses advanced techniques to mimic a real browser session, which is now ESSENTIAL for sites like YouTube.

**For YouTube & Private Sites: USE BROWSER COOKIES!**
1.  In the main window, under "Authentication," select the browser where you are logged into YouTube (e.g., Chrome).
2.  This allows the app to securely use your existing login session to defeat bot detection and access age-restricted or private content. It is the single most important step for reliable downloads.

**Troubleshooting Persistent SSL/Network Errors**
If errors continue even with browser cookies, the problem is your local environment.
1.  **Antivirus/Firewall:** Temporarily disable security software (including Windows Defender). If it works, add an exception for Python.
2.  **VPN/Proxy:** Disable any VPN or proxy services.
3.  **Update Engine:** Use the "Update Engine" button regularly to get the latest fixes.
4.  **Try a Different Network:** Use a mobile hotspot to check if your ISP or router is the issue.

**How to Download from Unsupported Sites (Manual Method)**
1.  Go to the video page, press F12 for Developer Tools -> "Network" tab.
2.  Filter for `.m3u8` and play the video.
3.  A link ending in `.m3u8` will appear. Right-click it -> Copy -> Copy link address.
4.  Paste this link into the downloader and click Download.
""")
        textbox.configure(state="disabled")
    def show(self): self.deiconify()

# --- 2. CONTROLLER LAYER ---
# Connects the View (UI) and the Service (backend).

class App(customtkinter.CTk):
    """
    The main application class that acts as the Controller, orchestrating the UI and Service layers.
    """
    def __init__(self):
        super().__init__()
        customtkinter.set_appearance_mode("Dark"); customtkinter.set_default_color_theme("blue")
       
        self.downloader = DownloaderService()
        self.ffmpeg_available = False
       
        self.log_viewer = LogViewerToplevel(self); self.help_viewer = HelpToplevel(self)
        self.ytdlp_logger = YtdlpLogger(self.log_viewer.add_log_message)
       
        callbacks = {
            "select_folder": self._select_folder, "start_over": self._start_over, "fetch": self._start_fetch,
            "download": self._start_download, "view_logs": self.log_viewer.show, "show_help": self.help_viewer.show,
            "update_engine": self._update_engine
        }
        self.ui = UIManager(self, callbacks)
        self._initial_checks()

    def _initial_checks(self):
        self._check_ffmpeg()
        if not self._check_internet():
            self.ui.set_status("Error: No internet connection. Please connect and restart.", is_error=True)
            self.ui.toggle_controls(False)

    def _check_ffmpeg(self):
        try:
            subprocess.run(['ffmpeg', '-version'], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            self.ffmpeg_available = True
        except (FileNotFoundError, subprocess.CalledProcessError):
            self.ffmpeg_available = False
            self.ui.notify_ffmpeg_missing()

    def _check_internet(self) -> bool:
        try: socket.create_connection(("8.8.8.8", 53), timeout=3); return True
        except OSError: return False

    def _select_folder(self):
        path = filedialog.askdirectory();
        if path: self.ui.download_path.set(path)

    def _start_over(self):
        self.ui.url_textbox.delete("1.0", "end"); self.ui._add_placeholder()
        self.ui.set_status("Ready"); self.ui.set_progress(0)
        self.ui.browser_cookie_var.set(UIManager.BROWSER_OPTIONS[0])

    def _run_in_thread(self, target_func: Callable, *args):
        if not self._check_internet():
            self.ui.set_status("Error: No internet connection.", is_error=True); return
        self.ui.toggle_controls(False); self.ui.set_progress(0)
        threading.Thread(target=target_func, args=args, daemon=True).start()

    def _update_engine(self):
        self.ui.set_status("Updating yt-dlp engine...")
        self.log_viewer.log_textbox.delete("1.0", "end")
        self._run_in_thread(self._update_worker)
       
    def _update_worker(self):
        try:
            # Use sys.executable to ensure we're using the correct python interpreter/pip
            command = [sys.executable, "-m", "pip", "install", "--upgrade", "yt-dlp", "--no-warn-script-location"]
            process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding='utf-8')
           
            # Stream output to logger in real-time
            for line in iter(process.stdout.readline, ''):
                self.ytdlp_logger.info(line.strip())
           
            process.wait()
           
            if process.returncode == 0:
                self.after(0, self.ui.set_status, "yt-dlp engine updated successfully!")
            else:
                self.after(0, lambda: self.ui.set_status("Update failed. Check logs for details.", is_error=True))
               
        except Exception as e:
            self.ytdlp_logger.error(f"Failed to execute update command: {e}")
            self.after(0, lambda: self.ui.set_status("An error occurred during update. See logs.", is_error=True))
        finally:
            self.after(0, self.ui.toggle_controls, True)

    def _get_urls(self) -> Optional[List[str]]:
        urls_text = self.ui.url_textbox.get("1.0", "end-1c").strip()
        if not urls_text or urls_text == self.ui.placeholder_text:
            self.ui.set_status("Error: Please paste at least one URL.", is_error=True)
            return None
        return [url for url in urls_text.splitlines() if url.strip()]

    def _start_fetch(self):
        urls = self._get_urls();
        if not urls: return
        self.ui.set_status("Fetching video information...")
        self._run_in_thread(self._fetch_worker, urls)

    def _fetch_worker(self, urls: List[str]):
        try:
            browser = self.ui.browser_cookie_var.get()
            video_data = self.downloader.get_video_info(urls, browser, self.ytdlp_logger)
            def update_ui():
                self.ui.url_textbox.delete("1.0", "end")
                if video_data:
                    self.ui.url_textbox.insert("1.0", "\n".join(v['url'] for v in video_data))
                    self.ui.set_status(f"Successfully fetched info for {len(video_data)} items.")
                else:
                    self.ui.set_status("Could not fetch info. Site may be unsupported or need cookies. See logs.", is_error=True)
                self.ui.toggle_controls(True)
            self.after(0, update_ui)
        except Exception as e:
            self.ytdlp_logger.error(f"A critical error occurred during fetch: {e}")
            self.after(0, lambda: (self.ui.set_status("A critical error occurred. See logs.", True), self.ui.toggle_controls(True)))

    def _start_download(self):
        urls = self._get_urls();
        if not urls: return
        self.ui.set_status("Preparing to download...")
        self._run_in_thread(self._download_worker, urls)

    def _download_worker(self, urls: List[str]):
        try:
            is_audio = self.ui.audio_format_var.get() != "none"
            dl_opts = {'progress_hooks': [self._progress_hook]}

            if is_audio:
                audio_format = self.ui.audio_format_var.get()
                path = os.path.join(self.ui.download_path.get(), audio_format.upper()); os.makedirs(path, exist_ok=True)
                dl_opts['outtmpl'] = os.path.join(path, '%(title)s.%(ext)s')
                dl_opts['format'] = 'bestaudio/best'
                dl_opts['postprocessors'] = [{'key': 'FFmpegExtractAudio', 'preferredcodec': audio_format}]
            else:
                path = os.path.join(self.ui.download_path.get(), "Video"); os.makedirs(path, exist_ok=True)
                dl_opts['outtmpl'] = os.path.join(path, '%(title)s.%(ext)s')
                if self.ffmpeg_available:
                     dl_opts.setdefault('postprocessors', []).extend([
                        {'key': 'FFmpegVideoConvertor', 'preferedformat': 'mp4'},
                        {'key': 'FFmpegMetadata', 'add_metadata': True}
                     ])
           
            browser = self.ui.browser_cookie_var.get()
            self.downloader.download_videos(urls, browser, dl_opts, self.ytdlp_logger)
            self.after(0, lambda: (self.ui.set_status("All downloads completed successfully!"), self.ui.set_progress(1.0)))

        except Exception as e:
             self.after(0, lambda: self.ui.set_status("Download failed. Check logs for details.", is_error=True))
        finally:
            self.after(0, self.ui.toggle_controls, True)

    def _progress_hook(self, d: Dict[str, Any]):
        if d['status'] == 'downloading':
            total = d.get('total_bytes') or d.get('total_bytes_estimate')
            if total:
                progress = d.get('downloaded_bytes', 0) / total
                self.after(0, self.ui.set_progress, progress)
                self.after(0, self.ui.set_status, f"Downloading: {progress:.1%} at {d.get('_speed_str', 'N/A')} (ETA: {d.get('_eta_str', 'N/A')})")
        elif d['status'] == 'finished':
            self.after(0, self.ui.set_status, "Finalizing download...")
        elif 'postprocessor' in d.get('info_dict', {}):
             self.after(0, self.ui.set_status, "Processing file (converting/merging)...")

if __name__ == "__main__":
    app = App()
    app.mainloop()
